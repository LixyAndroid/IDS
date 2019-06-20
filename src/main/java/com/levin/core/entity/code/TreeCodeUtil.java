package com.levin.core.entity.code;

import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.NearestCar;
import com.levin.excel.TransportTask;

import java.util.*;

public class TreeCodeUtil {
    private static final Random random = new Random(System.currentTimeMillis());

    public static TreeCode gencode(List<TransportTask> taskList, List<Driver> driverList, String fitnessType) {
        List<TransportTask> orderList = new ArrayList<>(taskList);
        double df = (double) taskList.size() / driverList.size();
        int c = driverList.size();
        int o = orderList.size();

        //随机订单分组
        int carNum = 0;
        for (int i = 0; i < c; i++) {
            int value = random.nextFloat() < df ? 1 : 0;
            //carCodes[i] = value;
            carNum += value;
        }

        Collections.shuffle(orderList);
        List<Integer> random = LayerCodeUtil.random(carNum, o);
        List<List<TransportTask>> orders = new ArrayList<>();
        splitOrders(orderList, random, orders);

        //寻找离每个订单组最近的车辆，每个订单组只能分配给一辆车
        Map<String, List<TransportTask>> temp = new HashMap<>();
        List<VehicleCode> vehicleCodeList = new ArrayList<>(c);
        List<Driver> ignores = new ArrayList<>();
        for (List<TransportTask> ol : orders) {
            if (ol == null || ol.size() == 0) {
                continue;
            }

            NearestCar nearestCar = DataLab.nearest(ol.get(0), ignores, driverList);
            double dis = nearestCar.getDistance();
            Driver car = nearestCar.getCar();
            for (int t = 1; t < ol.size(); t++) {
                TransportTask order = ol.get(t);
                nearestCar = DataLab.nearest(order, ignores, driverList);
                if (nearestCar.getDistance() < dis) {
                    dis = nearestCar.getDistance();
                    car = nearestCar.getCar();
                }
            }
            temp.put(car.getId(), ol);
            ignores.add(car);
        }

        for (Driver car : driverList) {
            if (temp.get(car.getId()) == null) {
                vehicleCodeList.add(new VehicleCode(car, 0, new ArrayList<>()));
            } else {
                List<TransportTask> list = temp.get(car.getId());
                List<OrderCode> orderCodeList = new ArrayList<>();
                List<TransportTask> tmp = new ArrayList<>();
                tmp.addAll(list);
                tmp.addAll(list);
                Collections.shuffle(tmp);
                Set<String> flag = new HashSet<>();
                for (TransportTask order : tmp) {
                    if (flag.contains(order.getId())) {
                        orderCodeList.add(new OrderCode(2, order));
                    } else {
                        orderCodeList.add(new OrderCode(1, order));
                    }
                    flag.add(order.getId());
                }
                VehicleCode vehicleCode = new VehicleCode(car, 1, orderCodeList);
                vehicleCodeList.add(vehicleCode);
            }
        }

        vehicleCodeList.add(new VehicleCode(new Driver(), 2, new ArrayList<>()));//虚拟车辆
        TreeCode solutionCode = new TreeCode(vehicleCodeList, fitnessType);
        while (solutionCode.getFitness() < 0) {
            solutionCode = gencode(taskList, driverList, fitnessType);
        }
        return solutionCode;
    }


    /**
     * 分割运输任务
     */
    private static void splitOrders(List<TransportTask> orderList, List<Integer> random, List<List<TransportTask>> orders) {
        int p = 0;
        for (int k : random) {
            int toIdx = Math.min(p + k, orderList.size());
            if (p >= orderList.size()) {
                p = orderList.size();
            }

            orders.add(orderList.subList(p, toIdx));
            p += k;
        }
    }

    /**
     * 贪心法生成一个初始解
     */
    public static SolutionCode saveGen(List<TransportTask> taskList, List<Driver> driverList, String fitnessType) {
        Map<String, TransportTask> temp = new HashMap<>();
        for (TransportTask order : taskList) {
            List<Driver> ignore = new ArrayList<>();
            Driver nearst = DataLab.nearest(order, ignore).getCar();
            while (temp.get(nearst.getId()) != null) {
                ignore.add(nearst);
                nearst = DataLab.nearest(order, ignore).getCar();
            }
            temp.put(nearst.getId(), order);
        }

        List<VehicleCode> vehicleCodeList = new ArrayList<>();
        for (Driver car : driverList) {
            TransportTask order = temp.get(car.getId());
            if (order == null) {
                vehicleCodeList.add(new VehicleCode(car, 0, new ArrayList<>()));
            } else {
                List<OrderCode> orderCodeList = new ArrayList<>();
                orderCodeList.add(new OrderCode(1, order));
                orderCodeList.add(new OrderCode(2, order));
                vehicleCodeList.add(new VehicleCode(car, 1, orderCodeList));
            }
        }
        vehicleCodeList.add(new VehicleCode(new Driver(), 2, new ArrayList<>()));//虚拟车辆
        SolutionCode solutionCode = new TreeCode(vehicleCodeList, fitnessType);
        while (solutionCode.getFitness() < 0) {
            solutionCode = saveGen(taskList, driverList, fitnessType);
        }
        return solutionCode;
    }
}
