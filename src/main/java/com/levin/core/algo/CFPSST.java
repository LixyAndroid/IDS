package com.levin.core.algo;

import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.TreeCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.core.entity.dto.ClusteringDto;
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

    /**
     * 分区列表
     */
    private List<PartitionDto> partitionList = new ArrayList<>();

    private List<SolutionCode> partitionResult = new ArrayList<>();

    public CFPSST(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, int size, String fitnessType, double alpha, double beta, int NN, int b1, int b2) {
        super(MAX_GEN, driverList, taskList, size, fitnessType, alpha, beta, NN);
        this.b1 = b1;
        this.b2 = b2;
    }

    public SolutionCode solve() {
        //分区
        partition();

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
        return new TreeCode(vehicleCodeList, fitnessType);
    }

    /**
     * 分区
     */
    public void partition() {
        //聚类
        clustering();
        List<Driver> ignore = new ArrayList<>();
        for (ClusteringDto clusteringDto : clusteringResult) {
            List<Driver> drivers = new ArrayList<>();
            for (TransportTask tt : clusteringDto.getAll()) {
                //找最近的pf辆车
                drivers.addAll(DataLab.nearest(tt, ignore, pf));
            }
            partitionList.add(new PartitionDto(drivers, clusteringDto.getAll()));
        }
    }

    /**
     * 分散搜索
     */
    public void evolution() {
        double sum = 0;
        for (PartitionDto partitionDto : partitionList) {
            //System.out.println("---------------------------");
            IDS solver = new SsIDS(MAX_GEN, partitionDto.getDriverList(), partitionDto.getTaskList(), size, fitnessType, b1, b2, NN, true, 2);
            SolutionCode solve = solver.solve();
            sum += solve.getFitness();
            partitionResult.add(solve);
        }
        System.out.println(sum);
    }


    public static void main(String[] args) {
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
                                                DataLab.taskList(path + "task.xls"), n, "distance", a, b, nn, b1, b2);
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
}
