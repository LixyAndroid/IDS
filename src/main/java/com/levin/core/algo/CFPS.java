package com.levin.core.algo;

import com.levin.core.clustring.Clustring;
import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.TreeCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.core.entity.dto.AddTaskDto;
import com.levin.core.entity.dto.ClusteringDto;
import com.levin.core.entity.fitness.FitnessFactory;
import com.levin.entity.CarProp;
import com.levin.entity.CarPropLab;
import com.levin.entity.Point;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.NearestCar;
import com.levin.excel.TransportTask;
import com.levin.util.BmapUtils;
import com.levin.util.FileUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 先聚类再分组
 */
public class CFPS extends IDS implements Clustring<TransportTask> {
    /**
     * I型聚类半径:A1->B1->A2->B2
     */
    protected double alpha;
    /**
     * II型聚类半径:A1->A2->B1->B2
     */
    protected double beta;

    /**
     * 聚类结果
     */
    protected List<ClusteringDto> clusteringResult = new ArrayList<>();

    /**
     * 剩余未分配的订单
     */
    protected Set<TransportTask> remain = new HashSet<>(taskList);

    /**
     * 已经分配的订单  车-->订单集合
     */
    protected Map<String, List<TransportTask>> map = new HashMap<>();

    /**
     * 禁忌表长度
     */
    protected int NN;


    public CFPS(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, int size, String fitnessType, double alpha, double beta, int NN) {
        super(MAX_GEN, driverList, taskList, size, fitnessType);
        this.alpha = alpha;
        this.beta = beta;
        this.NN = NN;
    }

    protected void init() {
        super.init();
    }

    @Override
    protected void populationInit() {
        //nothing to do
    }

    @Override
    public SolutionCode solve() {
        //初始化
        init();
        //还有任务未分配
        while (remain.size() > 0) {
            //分组
            clustering();

            //组内路径
            path();
        }

        //生成编码
        List<VehicleCode> vehicleCodeList = new ArrayList<>();
        for (Driver driver : driverList) {
            List<TransportTask> transportTasks = map.getOrDefault(driver.getId(), new ArrayList<>());
            List<OrderCode> orderCodeList = new ArrayList<>();
            int value = transportTasks.size() > 0 ? 1 : 0;
            for (TransportTask tt : transportTasks) {
                orderCodeList.add(new OrderCode(1, tt));
                orderCodeList.add(new OrderCode(2, tt));
            }

            VehicleCode vehicleCode = new VehicleCode(driver, value, orderCodeList);
            vehicleCode.calFitness(FitnessFactory.get(fitnessType));
            vehicleCodeList.add(vehicleCode);
        }
        SolutionCode solution = new TreeCode(vehicleCodeList, fitnessType);

        //tabu 优化每辆车的行车路线
        SolutionCode bestSolution = solution.bestNeighbor(NN,1);
        this.bestS = bestSolution;
        this.bestF = bestSolution.calFitness();
        return bestSolution;
    }

    @Override
    protected void evaluate() {
        //nothing to do
    }

    public void path() {
        //重置剩余订单列表
        remain = new HashSet<>();

        //已经使用车辆
        List<Driver> arranged = new ArrayList<>();
        for (Driver driver : driverList) {
            if (map.getOrDefault(driver.getId(), new ArrayList<>()).size() > 0) {
                DoubleSummaryStatistics collect = map.get(driver.getId()).stream().collect(Collectors.summarizingDouble(TransportTask::getPlatenNum));
                if (collect.getSum() > CarPropLab.get(driver.getType()).getG()) {
                    arranged.add(driver);
                }
            }
        }

        for (ClusteringDto path : clusteringResult) {
            if (path != null && path.getAll().size() > 0) {
                List<TransportTask> in = path.getIn();
                List<TransportTask> mid = path.getMid();
                List<TransportTask> out = path.getOut();

                //arrange in set
                for (TransportTask task : in) {
                    Driver car = DataLab.nearest(task, arranged).getCar();
                    CarProp carProp = CarPropLab.get(car.getType());
                    arranged.add(car);

                    List<TransportTask> list = new ArrayList<>();
                    if (task.getPlatenNum() > carProp.getG()) { //超载，分割
                        TransportTask task1 = task.clone();
                        task1.setPlatenNum(carProp.getG());
                        list.add(task1);

                        task.setPlatenNum(task.getPlatenNum() - carProp.getG());
                        map.put(car.getId(), list);
                        remain.add(task);
                        continue;
                    } else {
                        list.add(task.clone());
                    }

                    Double sum = task.getPlatenNum();
                    AddTaskDto atd = addTask(mid, carProp, list, sum, 0);
                    if (atd.getIdx() >= mid.size() && atd.getSum() < carProp.getG() && atd.getSum() > sum) {
                        addTask(out, carProp, list, atd.getSum(), 0);
                    }

                    map.put(car.getId(), list);
                }

                //arrange mid set
                for (int i = 0; i < mid.size(); i++) {
                    TransportTask task = mid.get(i);
                    if (task.getPlatenNum() == 0)
                        continue;
                    Driver car = DataLab.nearest(task, arranged).getCar();
                    CarProp carProp = CarPropLab.get(car.getType());
                    arranged.add(car);

                    List<TransportTask> list = new ArrayList<>();
                    if (task.getPlatenNum() > carProp.getG()) { //超载，分割
                        TransportTask task1 = task.clone();
                        task1.setPlatenNum(carProp.getG());
                        list.add(task1);

                        task.setPlatenNum(task.getPlatenNum() - carProp.getG());
                        i--;
                        map.put(car.getId(), list);
                        continue;
                    } else {
                        list.add(task.clone());
                        double sum = task.getPlatenNum();
                        task.setPlatenNum(0d);
                        AddTaskDto atd = addTask(out, carProp, list, sum, 1);
                        if (atd.getIdx() >= out.size() && atd.getSum() < carProp.getG()) {
                            addTask(mid, carProp, list, atd.getSum(), 0);
                        }
                    }

                    map.put(car.getId(), list);
                }

                for (TransportTask task : mid) {
                    if (task.getPlatenNum() > 0) {
                        remain.add(task);
                    }
                }

                for (TransportTask task : out) {
                    if (task.getPlatenNum() > 0) {
                        remain.add(task);
                    }
                }
            }
        }
    }

    private AddTaskDto addTask(List<TransportTask> tasks, CarProp carProp, List<TransportTask> list, double sum, int start) {
        while (start < tasks.size()) {
            TransportTask task2 = tasks.get(start);
            if (task2.getPlatenNum() == 0) {
                start++;
                continue;
            }

            if (sum + task2.getPlatenNum() > carProp.getG()) {//超载
                TransportTask task1 = task2.clone();
                task1.setPlatenNum(carProp.getG() - sum);
                list.add(task1);

                task2.setPlatenNum(sum + task2.getPlatenNum() - carProp.getG());
                sum = carProp.getG();
                break;
            } else {
                list.add(task2.clone());
                sum += task2.getPlatenNum();
                task2.setPlatenNum(0d);
            }
            start++;
        }
        return new AddTaskDto(start, sum);
    }

    @Override
    public void clustering() {
        //重置聚类结果
        clusteringResult = new ArrayList<>();
        int size = this.remain.size();

        //起点与起点的距离
        double[][] matrix1 = new double[size][size];
        //终点与终点的距离
        double[][] matrix2 = new double[size][size];
        //起点与终点的距离
        double[][] matrix3 = new double[size][size];

        for (int i = 0; i < size; i++) {
            matrix1[i][i] = 0;
            matrix2[i][i] = 0;
            matrix3[i][i] = -1;
            TransportTask task1 = taskList.get(i);
            Point start1 = new Point(task1.getLat1(), task1.getLng1());
            Point end1 = new Point(task1.getLat2(), task1.getLng2());
            for (int j = 0; j < i; j++) {
                TransportTask task2 = taskList.get(j);
                Point start2 = new Point(task2.getLat1(), task2.getLng1());
                Point end2 = new Point(task2.getLat2(), task2.getLng2());
                matrix1[i][j] = matrix1[j][i] = BmapUtils.getDistance(start1, start2);
                matrix2[i][j] = matrix2[j][i] = BmapUtils.getDistance(end1, end2);
                matrix3[i][j] = BmapUtils.getDistance(start1, end2);
                matrix3[j][i] = BmapUtils.getDistance(start2, end1);
            }
        }

        //已参与聚类的订单集合
        Set<String> arranged = new HashSet<>();
        for (int i = 0; i < size; i++) {
            TransportTask order = taskList.get(i);
            if (!arranged.contains(order.getId())) {
                ClusteringDto clusteringDto = new ClusteringDto();
                arranged.add(order.getId());
                clusteringDto.getAll().add(order);
                clusteringDto.getMid().add(order);
                for (int j = 0; j < size; j++) {
                    TransportTask order1 = taskList.get(j);
                    //起点和终点都相近的订单 或者起点与终点相近的订单
                    if (!arranged.contains(order1.getId()) && (matrix1[i][j] <= alpha && matrix2[i][j] <= alpha
                            || matrix3[i][j] <= beta || matrix3[j][i] <= beta)) {
                        arranged.add(order1.getId());
                        clusteringDto.getAll().add(order1);

                        if (matrix1[i][j] <= alpha && matrix2[i][j] <= alpha) { //起点与终点分别都相近
                            clusteringDto.getMid().add(order1);
                            continue;
                        }
                        if (matrix3[i][j] <= beta) { //起点与终点相近
                            clusteringDto.getIn().add(order1);
                            continue;
                        }

                        if (matrix3[j][i] <= beta) { //终点与起点相近
                            clusteringDto.getOut().add(order1);
                        }
                    }

                }
                clusteringResult.add(clusteringDto);
            }

        }
    }

    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        String path = FileUtils.getAppPath() + "/src/main/resources/";
        IDS solver = new CFPS(0, DataLab.driverList(path + "vehicle.xls"),
                DataLab.taskList(path + "task.xls",-1), 1, "distance", 50, 200, 100);
        SolutionCode solve = solver.solve();
        System.out.println(solve.getFitness());
        System.out.println(solve.print());
        System.out.println(System.currentTimeMillis()-l);

    }

}
