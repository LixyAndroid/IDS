package com.levin.core.algo;

import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.TreeCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.core.entity.dto.ClusteringDto;
import com.levin.core.entity.dto.DriverDto;
import com.levin.core.entity.dto.PartitionDto;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.util.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚类 + 分散搜索
 */
public class CFPSST extends CFPS {
    /**
     * 分散搜索参数
     */
    private int b1;
    private int b2;

    /**
     * 分区因子(平均每个订单可分配车辆数,向下取整)
     */
    private int pf = driverList.size() / taskList.size();
    private double gama;

    /**
     * 分区列表
     */
    private List<PartitionDto> partitionList = new ArrayList<>();

    private List<SolutionCode> partitionResult = new ArrayList<>();

    public CFPSST(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, int size, String fitnessType, double alpha, double beta, double gama, int NN, int b1, int b2) {
        super(MAX_GEN, driverList, taskList, size, fitnessType, alpha, beta, NN);
        this.b1 = b1;
        this.b2 = b2;
        this.gama = gama;
    }

    public SolutionCode solve() {
        //分区
        partition2();

        //分散搜索
        evolution();

        return mergeSolution();
    }

    public SolutionCode mergeSolution() {
        Map<String, List<OrderCode>> map = new HashMap<>();
        for (SolutionCode sc : partitionResult) {
            for (VehicleCode vc : sc.vehicleCodeList) {
                map.put(vc.getDriver().getId(), vc.getOrderCodeList());
            }
        }

        List<VehicleCode> vehicleCodeList = new ArrayList<>();
        for (Driver driver : driverList) {
            if (map.get(driver.getId()) != null) {
                vehicleCodeList.add(new VehicleCode(driver, 1, map.get(driver.getId())));
            } else {
                vehicleCodeList.add(new VehicleCode(driver, 0, new ArrayList<>(0)));
            }
        }
        TreeCode treeCode = new TreeCode(vehicleCodeList, fitnessType);
        treeCode.setFitness(bestF);
        return treeCode;
    }

    /**
     * 分区
     */
    public List<PartitionDto> partition() {
        //聚类
        clustering();
        List<Driver> ignore = new ArrayList<>();
        for (ClusteringDto clusteringDto : clusteringResult) {
            List<Driver> drivers = new ArrayList<>();
            for (TransportTask tt : clusteringDto.getAll()) {
                //找最近的pf辆车
                drivers.addAll(DataLab.nearest(tt, ignore, pf));
            }
            List<DriverDto> driverDtos = new ArrayList<>();
            for (Driver driver : drivers) {
                driverDtos.add(new DriverDto(driver, 0));
            }
            partitionList.add(new PartitionDto(driverDtos, clusteringDto.getAll()));
        }
        return partitionList;
    }

    public List<PartitionDto> partition2() {
        //先聚类
        clustering();

        for (ClusteringDto clusteringDto : clusteringResult) {
            List<DriverDto> drivers = new ArrayList<>();
            for (TransportTask task : clusteringDto.getAll()) {
                drivers.addAll(DataLab.nesrest(task, gama));
            }

            partitionList.add(new PartitionDto(drivers, clusteringDto.getAll()));
        }

        //去除重复使用的车辆
        for (Driver driver : driverList) {
            double minDis = Double.MAX_VALUE;
            for (PartitionDto partitionDto : partitionList) {
                List<DriverDto> driverList = partitionDto.getDriverList();
                for (DriverDto driverDto : driverList) {
                    if (driver.equals(driverDto.getDriver()) && driverDto.getDistance() < minDis) {
                        minDis = driverDto.getDistance();
                    }
                }
            }

            int t = 0;
            for (PartitionDto partitionDto : partitionList) {
                List<DriverDto> driverList = partitionDto.getDriverList();
                List<DriverDto> driverList2 = new ArrayList<>();
                for (DriverDto driverDto : driverList) {
                    if (driver.equals(driverDto.getDriver()) && driverDto.getDistance() > minDis)
                        continue;
                    if (driver.equals(driverDto.getDriver()) && driverDto.getDistance() == minDis && t++ > 0)
                        continue;
                    if (driverList2.size() > partitionDto.getTaskList().size()) //车辆数超过订单数则不再增加车辆
                        continue;
                    driverList2.add(driverDto);
                }
                partitionDto.setDriverList(driverList2);
            }
        }

        return partitionList;
    }

    /**
     * 分散搜索
     */
    public void evolution() {
        double sum = 0;
        for (PartitionDto partitionDto : partitionList) {
            if (partitionDto.getDrivers().size() == 0) {
                System.out.println("该分区没有足够车辆");
                continue;
            }

            IDS solver = new SsIDS(MAX_GEN, partitionDto.getDrivers(), partitionDto.getTaskList(), size, fitnessType, b1, b2, NN, true, 2);
            SolutionCode solve = solver.solve();
            sum += solve.getFitness();
            partitionResult.add(solve);
        }
        bestF = sum;
        System.out.println("最终结果:" + sum);
    }

    public static void testPara() {
        String path = FileUtils.getAppPath() + "/src/main/resources/";
        int[] np = new int[]{50, 100, 300};
        int[] alpaha = new int[]{0, 50, 100, 300};
        int[] NN = new int[]{0, 10, 100, 1000};
        int[] B = new int[]{10, 15, 30};

        try {
            for (int n : np) {
                for (int a : alpaha) {
                    for (int b : alpaha) {
                        for (int nn : NN) {
                            for (int b1 : B) {
                                for (int b2 : B) {
                                    StringBuilder str = new StringBuilder(n + "\t" + a + "\t" + b + "\t" + nn + "\t" + b1 + "\t" + b2 + "\t");
                                    for (int i = 0; i < 10; i++) {
                                        IDS solver = new CFPSST(5, DataLab.driverList(path + "vehicle.xls"),
                                                DataLab.taskList(path + "task.xls"), n, "distance", a, b, 100, nn, b1, b2);
                                        SolutionCode solve = solver.solve();
                                        str.append(solve.getFitness()).append("\t");
                                        DataLab.clear();
                                    }
                                    str.append("\n");
                                    //System.out.println(str);
                                    FileUtils.writeFile(path + "result_ss.txt", str.toString());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test() {
        String path = FileUtils.getAppPath() + "/src/main/resources/";
        IDS solver = new CFPSST(1, DataLab.driverList(path + "vehicle.xls"),
                DataLab.taskList(path + "task.xls"), 100, "distance", 50, 200, 200, 1000, 30, 30);
        SolutionCode solve = solver.solve();
        System.out.println(solve.getFitness());
        solve.print();
        DataLab.clear();
    }

    public static void main(String[] args) {
        test();
    }
}
