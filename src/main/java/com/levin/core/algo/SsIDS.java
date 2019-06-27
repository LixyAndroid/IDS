package com.levin.core.algo;

import com.levin.core.entity.code.*;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.core.entity.code.LayerCodeUtil;

import java.text.DecimalFormat;
import java.util.*;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * 分散搜索
 */
public class SsIDS extends IDS {

    private static DecimalFormat decimalFormat = new DecimalFormat("#.00");

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
     * 根据车辆数目和订单数目的比值，决定分配给每辆车的订单的均值
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


    public SsIDS(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, int size, String fitnessType, int b1, int b2, int NN, boolean isSplit) {
        super(MAX_GEN, driverList, taskList, size, fitnessType);
        this.b1 = b1;
        this.b2 = b2;
        this.NN = NN;
        this.isSplit = isSplit;
    }

    @Override
    protected void init() {
        //父类初始化方法
        super.init();
        //计算配车因子
        df = (double) taskList.size() / driverList.size();
        //种群初始化
        populationInit();
    }

    @Override
    protected void populationInit() {
        population = new ArrayList<>();
        if (isSplit) { //分割订单
            for (int i = 0; i < size; i++) {
                population.add(TreeCodeUtil.gencode(taskList, driverList, fitnessType));
            }
        } else {
            //节约法生成一个初始解
            population.add(TreeCodeUtil.saveGen(taskList, driverList, fitnessType));
            //随机生成初始解
            for (int i = 0; i < super.size - 1; i++) {
                population.add(TreeCodeUtil.gencode(taskList, driverList, fitnessType));
            }
        }

    }

    @Override
    public SolutionCode solve() {
        //初始化
        init();
        for (int i = 0; i < MAX_GEN; i++) {
            //从初始解集中产生参考集 B = B1(多样性解)+B2（优质解）
            genRefer();
            //解的评价
            evaluate();

            //禁忌搜索优化（车辆分配方案不变，只优化装载方案）
            List<SolutionCode> refer2 = new ArrayList<>();
            for (SolutionCode sc : referSet) {
                refer2.add(sc.bestNeighbor(NN, 1));
            }
            referSet = refer2;
            evaluate();

            //子集产生与合并
            segReg();
        }
        return bestS;
    }

    @Override
    protected void evaluate() {
        for (SolutionCode sc : referSet) {
            if (sc.getFitness() < bestF && sc.getFitness() > 0) { //此处默认目标函数为最小,如果是最大化问题可使用倒数
                bestF = sc.getFitness();
                bestS = sc;
            }
        }
    }

    /**
     * 子集产生与重组
     */
    private void segReg() {
        //清空备选解集
        population = new ArrayList<>();
        //精英保留
        population.add(bestS);

        for (int i = 0; i < referSet.size(); i++) {
            for (int j = 0; j < i; j++) {
                //选取两个解组成解集对，生成两个新的解
                List<SolutionCode> solutionCodes = reOrg(referSet.get(i), referSet.get(j));

                if (solutionCodes.size() > 0) {
                    for (SolutionCode solutionCode : solutionCodes) {
                        if (solutionCode == null) {
                            continue;
                        }
                        //将生成的新解加入到备选解集中
                        if (solutionCode.getFitness() > 0) {
                            population.add(solutionCode);
                        } else {
                            //对于不合法(超载)的解进行修复
                            SolutionCode sc = solutionCode.repair();
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

        //如果备选解集的数量少于参考集大小，直接将备选集作为参考集
        if (population.size() <= (b1 + b2)) {
            referSet = population;
            return;
        }

        //优质解
        List<SolutionCode> solutionCodes = population.subList(0, b1);

        referSet.addAll(solutionCodes);

        //按多样性进行排序
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
            sqs(sqs1, vc1);
            sqs(sqs2, vc2);
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

        return Double.parseDouble(decimalFormat.format(-Math.log(x / (2 - x))));
    }

    private void sqs(Set<String> sqs2, VehicleCode vc2) {
        if (vc2.getOrderCodeList() != null) {
            int size = vc2.getOrderCodeList().size();
            for (int j = 1; j < size; j++) {
                sqs2.add(vc2.getOrderCodeList().get(j - 1).getTask().getId() + "," +
                        vc2.getOrderCodeList().get(j).getTask().getId());
            }
        }
    }

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
            List<Integer> list = LayerCodeUtil.random((int) Math.min(n / 1.5, temp.size() / 1.5), temp.size());
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
                List<OrderCode> insert = new ArrayList<>();
                for (OrderCode oc : ocs) {
                    insert.add(oc);
                    insert.add(new OrderCode(2, oc.getTask()));
                }

                if (random.nextFloat() < 0.1) {  //10%的概率让该订单不参与运输
                    List<OrderCode> orderCodeList = vehicleCodeList.get(n - 1).getOrderCodeList();
                    if (orderCodeList == null)
                        vehicleCodeList.set(n - 1, new VehicleCode(vehicleCodeList.get(nextInt).getDriver(), 1, insert));
                } else
                    vehicleCodeList.set(nextInt, new VehicleCode(vehicleCodeList.get(nextInt).getDriver(), 1, insert));
            }
        }
        return new TreeCode(vehicleCodeList, fitnessType);
    }
}
