package com.levin.excel;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.levin.core.entity.dto.DriverDto;
import com.levin.entity.Point;
import com.levin.util.BmapUtils;
import com.levin.util.FileUtils;

import java.io.File;
import java.util.*;

public class DataLab {
    private static List<Driver> driverList = null;
    private static List<TransportTask> transportTaskList = null;
    private static Map<String, Integer> map = null;
    /**
     * 距离矩阵
     */
    public static double[][] distance;

    /**
     * 车辆平均滞留时间
     */
    private static double avgTimeOfCar = 0;

    /**
     * 从excel文件获取司机列表
     *
     * @param path 文件路径
     * @return List<Driver>
     */
    public static List<Driver> driverList(String path) {
        if (driverList != null && driverList.size() > 0) {
            return driverList;
        }

        File file = new File(path);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        driverList = ExcelImportUtil.importExcel(file, Driver.class, new ImportParams());
        return driverList;
    }

    /**
     * 从excel文件获取任务列表
     *
     * @param path 文件路径
     * @return List<TransportTask>
     */
    public static List<TransportTask> taskList(String path) {
        if (transportTaskList != null && transportTaskList.size() > 0) {
            return transportTaskList;
        }

        File file = new File(path);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        transportTaskList = ExcelImportUtil.importExcel(file, TransportTask.class, new ImportParams());
        //transportTaskList = transportTaskList.subList(90, 120);
        return transportTaskList;
    }

    /**
     * 生成距离矩阵
     */
    public static double[][] genDisMatrix() {
        if (driverList == null || transportTaskList == null || distance != null && distance.length > 0) {
            return distance;
        }

        List<Point> pointList = new ArrayList<>();
        int p = 0;
        map = new HashMap<>();
        for (Driver car : driverList) {
            pointList.add(new Point(car.getLat(), car.getLng()));
            map.put(String.valueOf(car.getId()), p++);
        }

        for (TransportTask order : transportTaskList) {
            pointList.add(new Point(order.getLat1(), order.getLng1()));
            pointList.add(new Point(order.getLat2(), order.getLng2()));
            map.put("O" + order.getId(), p);
            p += 2;
        }

        int size = driverList.size() + 2 * transportTaskList.size();
        distance = new double[size][size];
        for (int i = 0; i < size; i++) {
            distance[i][i] = 0;
            for (int j = 0; j < i; j++) {
                Point X = pointList.get(i);
                Point Y = pointList.get(j);
                distance[i][j] = distance[j][i] = BmapUtils.getDistance(X, Y);
            }
        }
        return distance;
    }

    /**
     * type = 0,1,2
     */
    public static int getIdx(String s, int type) {
        s = s.split("_")[0];
        if (map == null) {
            genDisMatrix();
        }

        if (type == 1) { //取货位置
            return map.get("O" + s);
        } else if (type == 2) { //送货位置
            return map.get("O" + s) + 1;
        }
        return map.get(s); //车辆位置
    }

    public static double getDistance(int i, int j) {
        if (distance == null) {
            genDisMatrix();
        }

        return distance[i][j];
    }

    /**
     * 获取离订单最近的车
     */
    public static NearestCar nearest(TransportTask order, List<Driver> ignore) {
        return nearest(order, ignore, driverList);
    }

    public static NearestCar nearest(TransportTask order, List<Driver> ignore, List<Driver> drivers) {
        String id = order.getId().split("_")[0];
        int idx = getIdx(id, 1);
        double dis = Double.MAX_VALUE;
        Driver res = drivers.get(0);
        for (Driver car : drivers) {
            int curr = getIdx(car.getId(), 0);
            double distance = getDistance(idx, curr);
            if (distance < dis && distance >= 0 && !ignore.contains(car)) {
                dis = distance;
                res = car;
            }
        }
        return new NearestCar(res, dis);
    }

    /**
     * 获取离订单最近的k辆车
     */
    public static List<Driver> nearest(TransportTask task, List<Driver> ignore, int k) {
        List<Driver> result = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            NearestCar nearest = nearest(task, ignore);
            result.add(nearest.getCar());
            ignore.add(nearest.getCar());
        }
        return result;
    }

    /**
     * 查找某一订单一定距离内的车辆
     */
    public static List<DriverDto> nesrest(TransportTask task, double gama) {
        List<DriverDto> result = new ArrayList<>();
        int idx = getIdx(task.getId(), 1);
        for (Driver driver : driverList) {
            double dis = getDistance(getIdx(driver.getId(), 0), idx);
            if (dis < gama) {
                result.add(new DriverDto(driver, dis));
            }
        }
        return result;
    }

    public static TransportTask nearest(Driver driver) {
        TransportTask task = transportTaskList.get(0);
        double dis = Double.MAX_VALUE;
        int idx = getIdx(driver.getId(), 0);
        for (TransportTask tt : transportTaskList) {
            double tmp = getDistance(idx, getIdx(tt.getId(), 1));
            if (tmp < dis) {
                dis = tmp;
                task = tt;
            }
        }
        return task;
    }

    public static void clear() {
        map = null;
        driverList = null;
        transportTaskList = null;
        distance = null;
    }

    public static void main(String[] args) {
        String path = FileUtils.getAppPath() + "/src/main/resources";
        List<Driver> drivers = driverList(path + "/vehicle.xls");
        System.out.println(drivers.size());
    }

    public static Driver getDriver(String code) {
        for (Driver driver : driverList) {
            if (driver.getId().equalsIgnoreCase(code))
                return driver;
        }
        return null;
    }

    public static TransportTask getTask(String code) {
        for (TransportTask tt : transportTaskList) {
            if (tt.getId().equalsIgnoreCase(code))
                return tt;
        }
        return null;
    }

    public static double getAvgTimeOfCar() {
        if (avgTimeOfCar > 0)
            return avgTimeOfCar;
        else {
            double sum = 0;
            for (Driver driver : driverList) {
                sum += driver.getRetention();
            }
            return sum / driverList.size();
        }
    }
}
