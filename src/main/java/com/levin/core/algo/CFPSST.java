package com.levin.core.algo;

import com.levin.core.entity.code.*;
import com.levin.core.entity.dto.ClusteringDto;
import com.levin.core.entity.dto.DriverDto;
import com.levin.core.entity.dto.PartitionDto;
import com.levin.entity.CarPropLab;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.util.FileUtils;
import com.levin.core.entity.code.LayerCodeUtil;

import java.util.*;

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
        super.init();
        //分区
        partition2();

        //优化每个分区
        evolution();

        return mergeSolution();
    }

    private SolutionCode mergeSolution() {
        Map<String, List<OrderCode>> map = new HashMap<>();
        for (SolutionCode sc : partitionResult) {
            for (VehicleCode vc : sc.vehicleCodeList) {
                if (vc.getValue() == 2)
                    continue;

                if (vc.getOrderCodeList() != null && vc.getOrderCodeList().size() > 0) {
                    List<OrderCode> orderCodes = map.getOrDefault(vc.getDriver().getId(),new ArrayList<>());
                    orderCodes.addAll(vc.getOrderCodeList());
                    map.put(vc.getDriver().getId(), orderCodes);
                }
            }
        }

        List<VehicleCode> vehicleCodeList = new ArrayList<>();
        for (Driver driver : driverList) {
            if (map.get(driver.getId()) != null && map.get(driver.getId()).size() > 0) {
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
     * 优化每个分区
     */
    private void evolution() {
        double sum = 0;
        for (PartitionDto partitionDto : partitionList) {
            System.out.println("===============");
            if (partitionDto.getDrivers().size() == 0) {
                System.out.println("该分区没有足够车辆,可在 \'" + partitionDto.getTaskList().get(0).getStart() + "\' 安排新的车辆");
                continue;
            }

            //订单切分
            List<TransportTask> taskList = randomSplit(partitionDto);

            //按分区内车辆数和订单数选择不同算法
            if (taskList.size() < 3 || partitionDto.getDrivers().size() < 3) { //禁忌搜索
                SolutionCode solutionCode = TreeCodeUtil.gencode(taskList, driverList, fitnessType);
                Set<SolutionCode> set = new HashSet<>(NN);  //禁忌表
                set.add(solutionCode);
                double bf = solutionCode.getFitness();
                SolutionCode bs = solutionCode;
                for (int i = 0; i < NN; i++) {
                    solutionCode = TreeCodeUtil.gencode(taskList, driverList, fitnessType);
                    if (!set.contains(solutionCode) && solutionCode.getFitness() < bf) {
                        bf = solutionCode.getFitness();
                        bs = solutionCode;
                    }
                    System.out.println(sum+bf);
                    set.add(solutionCode);
                }
                sum += bf;

                partitionResult.add(bs);
            } else { //分散搜索
                IDS solver = new SsIDS(MAX_GEN, partitionDto.getDrivers(), taskList, size, fitnessType, b1, b2, NN, true);
                SolutionCode solve = solver.solve();
                sum += solve.getFitness();
                partitionResult.add(solve);
            }

        }
        bestF = sum;
        System.out.println("最终结果:" + sum);
    }

    /**
     * 随机切分
     */
    private List<TransportTask> randomSplit(PartitionDto partitionDto) {
        double minWeight = Double.MAX_VALUE;

        for (Driver driver : partitionDto.getDrivers()) {
            double weight = CarPropLab.get(driver.getType()).getG();

            if (weight < minWeight) {
                minWeight = weight;
            }
        }

        List<TransportTask> taskListAfterSplit = new ArrayList<>();
        for (TransportTask tt : partitionDto.getTaskList()) {
            if (tt.getPlatenNum() < minWeight) {
                taskListAfterSplit.add(tt);
            } else {
                int plateNum = (int) Math.ceil(tt.getPlatenNum());
                int num = random.nextInt(plateNum);
                List<Integer> random = LayerCodeUtil.random(num, plateNum);

                String id = tt.getId();
                for (int i = 0; i < num; i++) {
                    TransportTask task = tt.clone();
                    task.setId(id + "_" + i); //切分后订单的id
                    task.setPlatenNum((double) random.get(i));   //切分后订单的需求量
                    task.setAmount(tt.getAmount() * task.getPlatenNum() / tt.getPlatenNum());  //切分后订单的金额等比例切分
                    taskListAfterSplit.add(task);
                }
            }

        }
        return taskListAfterSplit;
    }


    /**
     * 按单位切分
     */
    private List<TransportTask> unitSplit(PartitionDto partitionDto) {
        List<TransportTask> taskListAfterSplit = new ArrayList<>();
        for (TransportTask tt : partitionDto.getTaskList()) {
            int splitNum = (int) Math.ceil(tt.getPlatenNum());
            for (int i = 0; i < splitNum; i++) {
                TransportTask task = tt.clone();
                task.setId(tt.getId() + "_" + i);
                task.setPlatenNum(1D);
                taskListAfterSplit.add(task);
            }
        }
        return taskListAfterSplit;
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
        IDS solver = new CFPSST(3, DataLab.driverList(path + "vehicle.xls"),
                DataLab.taskList(path + "task.xls"), 100, "distance", 50, 200, 50, 1000, 30, 30);
        SolutionCode solve = solver.solve();
        System.out.println(solve.getFitness());
        System.out.println(solve.print());
        DataLab.clear();
    }

    public static void main(String[] args) {
        test();
    }
}
