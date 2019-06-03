package com.levin.core.algo;

import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.TreeCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.entity.CarPropLab;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.NearestCar;
import com.levin.excel.TransportTask;

import java.text.DecimalFormat;
import java.util.*;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * 分散搜索
 */
public class SsIDS extends IDS {


    /**
     * 参考集
     */
    private List<SolutionCode> referSet = new ArrayList<>();

    /**
     * 参考集中多样性解个数
     */
    private int b1;
    /**
     * 参考集中优质解个数
     */
    private int b2;


    /**
     * 配车因子（直接影响使用车辆数目）
     */
    private double df;

    /**
     * 禁忌表长度
     */
    private int NN;

    /**
     * 是否拆分
     */
    private boolean isSplit;

    private int splitType;

    public SsIDS(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, int size, String fitnessType, int b1, int b2, int NN, boolean isSplit, int splitType) {
        super(MAX_GEN, driverList, taskList, size, fitnessType);
        this.b1 = b1;
        this.b2 = b2;
        this.NN = NN;
        this.isSplit = isSplit;
        this.splitType = splitType;
    }

    @Override
    protected void init() {
        super.init();
        df = (double) taskList.size() / driverList.size();
        populationInit();
    }

    @Override
    protected void populationInit() {
        population = new ArrayList<>();
        if (isSplit) { //分割订单
            for (int i = 0; i < size; i++) {
                population.add(genCode());
            }
        } else {
            //节约法生成一个初始解
            population.add(saveGen());
            //随机生成初始解
            for (int i = 0; i < super.size - 1; i++) {
                population.add(genCode());
            }
        }

    }

    @Override
    public SolutionCode solve() {
        System.out.println("come in ... ");
        if (taskList.size() < 3 && driverList.size() < 3) { //邻域搜索
            System.out.println("neighbor search");
            super.init();
            df = (double) taskList.size() / driverList.size();
            SolutionCode solutionCode = genCode();
            bestS = solutionCode;
            bestF = solutionCode.getFitness();
            for (int i = 0; i < NN; i++) {
                solutionCode = genCode();
                if (solutionCode.getFitness() < bestF) {
                    bestF = solutionCode.getFitness();
                    bestS = solutionCode;
                }
            }
            System.out.println(bestF);
        } else { //分散搜索
            System.out.println("ss search ");
            //产生初始解集
            init();
            for (int i = 0; i < MAX_GEN; i++) {
                //从初始解集中产生参考集 B = B1(多样性解)+B2（优质解）
                genRefer();
                evaluate();

                //禁忌搜索优化（车辆分配方案不变，只优化装载方案）
                List<SolutionCode> refer2 = new ArrayList<>();
                for (SolutionCode sc : referSet) {
                    refer2.add(sc.bestSolution(NN));
                }
                referSet = refer2;
                evaluate();

                //子集产生与合并
                segReg();
            }
        }

        return bestS;
    }

    @Override
    protected void evaluate() {
        for (SolutionCode sc : referSet) {
            if (sc.getFitness() < bestF && sc.getFitness() > 0) {
                bestF = sc.getFitness();
                bestS = sc;
            }
        }
        System.out.println(bestF);
    }

    /**
     * 子集产生与重组
     */
    private void segReg() {
        population = new ArrayList<>();
        population.add(bestS);  //精英保留

        for (int i = 0; i < referSet.size(); i++) {
            for (int j = 0; j < i; j++) {
                //System.out.println(i + "---" + j);
                List<SolutionCode> solutionCodes = reOrg(referSet.get(i), referSet.get(j));

                if (solutionCodes.size() > 0) {
                    for (SolutionCode solutionCode : solutionCodes) {
                        if (solutionCode == null) {
                            System.out.println("---");
                            continue;
                        }

                        if (solutionCode.getFitness() > 0) {
                            population.add(solutionCode);
                        } else {
                            SolutionCode sc = solutionCode.repair();
                            //System.out.println(sc.print());
                            population.add(sc);
                        }
                    }

                }

            }
        }
    }

    /**
     * 生成参考集
     */
    private void genRefer() {
        referSet = new ArrayList<>();
        //B = B1 + B2
        // 计算每个编码与其他所有编码的多样性距离，选取多样性距离最大的前B1个解作为多样性解
        double[][] varDis = new double[population.size()][population.size()];
        for (int i = 0; i < population.size(); i++) {
            varDis[i][i] = 0;
            for (int j = 0; j < i; j++) {
                double v = CalVarDis(population.get(i), population.get(j));
                varDis[i][j] = varDis[j][i] = v;
            }
        }

        //计算平均多样性距离
        for (int i = 0; i < population.size(); i++) {
            double[] di = varDis[i];
            SolutionCode sc = population.get(i);
            double dd = 0;
            for (double d : di) {
                dd += d;
            }

            sc.setDd(Math.abs(dd));
        }

        //按最优质排序
        population = population.stream().collect(
                collectingAndThen(
                        toCollection(() -> new TreeSet<>(comparingDouble(SolutionCode::getFitness))), ArrayList::new)
        );

        if (population.size() <= (b1 + b2)) {
            referSet = population;
            return;
        }

        //优质解
        List<SolutionCode> solutionCodes = population.subList(0, b1);

        referSet.addAll(solutionCodes);

        List<SolutionCode> Np_B1 = population.subList(b1, population.size());
        Np_B1.sort((o1, o2) -> {
            if (o1.getDd() == o2.getDd())
                return 0;

            return o1.getDd() > o2.getDd() ? -1 : 1;
        });

        //多样性解
        referSet.addAll(Np_B1.subList(1, b2 + 1));

    }

    /**
     * 计算多样性距离
     */
    private double CalVarDis(SolutionCode s1, SolutionCode s2) {
        //Ds = 1-(k1*n1/N1+k2*n2/N2) k1+k2 = 1
        int sco = 0;//分配到相同车辆的订单数
        int c2u = 0; //使用的车辆数
        int cn = driverList.size();
        int on = taskList.size();
        Set<String> sqs1 = new HashSet<>(); //编码1相邻序列对集合
        Set<String> sqs2 = new HashSet<>(); //编码2相邻序列对集合


        for (int i = 0; i < cn; i++) {
            VehicleCode vc1 = s1.vehicleCodeList.get(i);
            VehicleCode vc2 = s2.vehicleCodeList.get(i);
            c2u += vc1.getValue();

            if (vc1.getOrderCodeList() != null && vc2.getOrderCodeList() != null) {
                for (OrderCode oc : vc1.getOrderCodeList()) {
                    if (oc.getType() == 1 && vc2.getOrderCodeList().contains(oc)) {
                        sco++;
                    }
                }
            }

            if (vc1.getOrderCodeList() != null) {
                int size = vc1.getOrderCodeList().size();
                for (int j = 1; j < size; j++) {
                    sqs1.add(vc1.getOrderCodeList().get(j - 1).getTask().getId() + "," +
                            vc1.getOrderCodeList().get(j).getTask().getId());
                }
            }

            if (vc2.getOrderCodeList() != null) {
                int size = vc2.getOrderCodeList().size();
                for (int j = 1; j < size; j++) {
                    sqs2.add(vc2.getOrderCodeList().get(j - 1).getTask().getId() + "," +
                            vc2.getOrderCodeList().get(j).getTask().getId());
                }
            }

        }

        int sqn = 0;  //相同相邻序列对数目
        for (String sq : sqs1) {
            if (sqs2.contains(sq)) {
                sqn++;
            }
        }

        double x = 1 - (0.5 * sco / on + 0.5 * sqn / (2 * on - c2u));
        if (x <= 0) {
            x = 0.01;
        }
        if (x >= 1) {
            x = 0.99;
        }

        if (Double.isNaN(x)) {
            return 0;
        }

        return Double.parseDouble(decimalFormat.format(-Math.log(1 / x - 1)));
    }

    private static DecimalFormat decimalFormat = new DecimalFormat("#.00");

    /**
     * 子集产生与重组
     */
    private List<SolutionCode> reOrg(SolutionCode sc1, SolutionCode sc2) {
        int size = sc1.vehicleCodeList.size();
        int theta = random.nextInt(size);

        List<VehicleCode> part11 = sc1.vehicleCodeList.subList(0, theta);
        List<VehicleCode> part12 = sc1.vehicleCodeList.subList(theta, size);
        List<VehicleCode> part21 = sc2.vehicleCodeList.subList(0, theta);
        List<VehicleCode> part22 = sc2.vehicleCodeList.subList(theta, size);

        List<OrderCode> all = new ArrayList<>();
        for (VehicleCode vc : sc1.vehicleCodeList) {
            if (vc.getOrderCodeList() != null) {
                all.addAll(vc.getOrderCodeList());
            }
        }

        List<SolutionCode> result = new ArrayList<>();
        SolutionCode combinate1 = combinate(part11, part22, all);
        SolutionCode combinate2 = combinate(part21, part12, all);

        result.add(combinate1);
        result.add(combinate2);

        return result;
    }

    /**
     * 路径重连
     */
    private SolutionCode combinate(List<VehicleCode> part1, List<VehicleCode> part2, List<OrderCode> all) {
        List<OrderCode> allCode = new ArrayList<>(all);
        Set<OrderCode> tmp = new HashSet<>();
        List<VehicleCode> vehicleCodeList = new ArrayList<>();

        for (VehicleCode vc : part1) {
            if (vc.getOrderCodeList() != null) {
                tmp.addAll(vc.getOrderCodeList());
            }
            vehicleCodeList.add(new VehicleCode(vc.getDriver(), vc.getValue(), vc.getOrderCodeList()));
        }

        for (VehicleCode vc : part2) {
            List<OrderCode> orderCodeList = null;
            if (vc.getOrderCodeList() != null) {
                orderCodeList = new ArrayList<>();
                for (OrderCode oc : vc.getOrderCodeList()) {
                    if (!tmp.contains(oc)) {
                        orderCodeList.add(oc);
                        tmp.add(oc);
                    }
                }
            }
            if (orderCodeList != null && orderCodeList.size() > 0) {
                vehicleCodeList.add(new VehicleCode(vc.getDriver(), 1, orderCodeList));
            } else {
                vehicleCodeList.add(new VehicleCode(vc.getDriver(), 0, orderCodeList));
            }

        }

        allCode.removeAll(tmp);
        if (allCode.size() > 0) {
            List<OrderCode> temp = new ArrayList<>();
            for (OrderCode oc : allCode) {
                if (oc.getType() == 1) {
                    temp.add(oc);
                }
            }

            int n = part1.size() + part2.size();
            List<Integer> list = random((int) Math.min(n / 1.5, temp.size() / 1.5), temp.size());
            List<List<OrderCode>> remain = new ArrayList<>();
            int p = 0;
            for (int k : list) {
                int toIdx = p + k < temp.size() ? p + k : temp.size();
                if (p >= temp.size()) {
                    p = temp.size();
                }
                remain.add(temp.subList(p, toIdx));
                p += k;
            }

            for (List<OrderCode> ocs : remain) {
                int nextInt = random.nextInt(n - 2 > 0 ? n - 2 : 1);
                //while (!(vehicleCodeList.get(nextInt) == null || vehicleCodeList.get(nextInt).getOrderCodeList() == null || vehicleCodeList.get(nextInt).getOrderCodeList().size() == 0)) {
                //nextInt = random.nextInt(n - 2 > 0 ? n - 2 : 1);
                //}
                List<OrderCode> insert = new ArrayList<>();
                for (OrderCode oc : ocs) {
                    insert.add(oc);
                    insert.add(new OrderCode(2, oc.getTask()));
                }

                vehicleCodeList.set(nextInt, new VehicleCode(vehicleCodeList.get(nextInt).getDriver(), 1, insert));
            }


        }

        return new TreeCode(vehicleCodeList, fitnessType);
    }

    /**
     * 随机切分
     */
    private List<TransportTask> randomSplit() {
        double minWeight = Double.MAX_VALUE;

        for (Driver driver : driverList) {
            double weight = CarPropLab.get(driver.getType()).getG();

            if (weight < minWeight) {
                minWeight = weight;
            }
        }

        List<TransportTask> taskListAfterSplit = new ArrayList<>();
        for (TransportTask tt : taskList) {
            if (tt.getPlatenNum() < minWeight) {
                taskListAfterSplit.add(tt);
            } else {
                int plateNum = (int) Math.ceil(tt.getPlatenNum());
                int num = random.nextInt(plateNum);
                List<Integer> random = random(num, plateNum);

                String id = tt.getId();
                for (int i = 0; i < num; i++) {
                    TransportTask task = tt.clone();
                    task.setId(id + "_" + i);
                    task.setPlatenNum((double) random.get(i));
                    taskListAfterSplit.add(task);
                }
            }

        }
        return taskListAfterSplit;
    }

    /**
     * 按单位切分
     */
    private List<TransportTask> unitSplit() {
        List<TransportTask> taskListAfterSplit = new ArrayList<>();
        for (TransportTask tt : taskList) {
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

    /**
     * 产生一个解编码
     */
    private SolutionCode genCode() {
        List<TransportTask> orderList;
        if (isSplit) { //切分
            if (splitType == 1) {
                orderList = unitSplit();
            } else {
                orderList = randomSplit();
            }

        } else {
            orderList = new ArrayList<>(taskList);
        }

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
        List<Integer> random = random(carNum, o);
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

        SolutionCode solutionCode = new TreeCode(vehicleCodeList, fitnessType);
        while (solutionCode.getFitness() < 0) {
            solutionCode = genCode();
        }
        return solutionCode;
    }

    /**
     * 分割运输任务
     */
    private void splitOrders(List<TransportTask> orderList, List<Integer> random, List<List<TransportTask>> orders) {
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
     * 随机分段
     */
    private List<Integer> random(int n, int L) {
        List<Integer> res = new ArrayList<>();
        int nn = n;
        for (int i = 0; i < n - 1; i++) {
            if (L <= 0) {
                res.add(0);
            } else {
                int a = Math.max(1, random.nextInt(Math.max(L / nn + 1, 1)));
                int b = random.nextInt(a) + L / nn;
                L = L - b;
                if (L <= 0) {
                    res.add(L + b);
                } else {
                    res.add(b);
                }
            }
            nn--;
        }

        res.add(L < 0 ? 0 : L);
        Collections.shuffle(res);
        return res;
    }

    /**
     * 贪心法生成一个初始解
     */
    private SolutionCode saveGen() {
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
        SolutionCode solutionCode = new TreeCode(vehicleCodeList, fitnessType);
        while (solutionCode.getFitness() < 0) {
            solutionCode = saveGen();
        }
        return solutionCode;
    }

}
