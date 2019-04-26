package com.levin.excel;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
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
        int idx = getIdx(order.getId(), 1);
        double dis = Double.MAX_VALUE;
        Driver res = driverList.get(0);
        for (Driver car : driverList) {
            int curr = getIdx(car.getId(), 0);
            double distance = getDistance(idx, curr);
            if (distance < dis && distance >= 0 && !ignore.contains(car)) {
                dis = distance;
                res = car;
            }
        }
        return new NearestCar(res, dis);
    }

    public static void main(String[] args) {
        String path = FileUtils.getAppPath() + "/src/main/resources";
        List<Driver> drivers = driverList(path + "/vehicle.xls");
        System.out.println(drivers.size());
    }
}
