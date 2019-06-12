package com.levin.core.algo;

import com.levin.core.entity.code.LayerCode;
import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.entity.Gene;
import com.levin.entity.GLink;
import com.levin.entity.SO;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.util.FileUtils;
import com.levin.util.IdsUtil;

import java.util.*;

/**
 * 蜂群算法求解VRP
 */
public class ABCIDS extends IDS {
    /**
     * 蜂群大小
     */
    private int NP = 30;

    /**
     * 实物源数量
     */
    private int FN = NP / 2;

    /**
     * 觅食周期(停止条件)
     */
    private int max_gen = 2500;

    /**
     * 无法通过limit次搜索改进的实物源被抛弃
     */
    private int limit = 100;

    /**
     * 目标函数值
     */
    private double[] f;

    /**
     * 适应度值
     */
    private double[] fitness;

    /**
     * 在trial次数之后无法改进的向量
     */
    private double[] trial;

    /**
     * 一个食物源（解决方案）被选择的概率的向量
     */
    private double[] prob;


    /**
     * min constructor
     *
     * @param MAX_GEN     最大迭代次数
     * @param driverList  司机列表
     * @param taskList    任务列表
     * @param fitnessType 目标函数值类型
     */
    public ABCIDS(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, String fitnessType) {
        super(MAX_GEN, driverList, taskList, 0, fitnessType);
        fitness = new double[FN];
        f = new double[FN];
        trial = new double[FN];
        prob = new double[FN];
    }

    /**
     * general constructor
     *
     * @param MAX_GEN     最大迭代次数
     * @param driverList  司机列表
     * @param taskList    任务列表
     * @param fitnessType 目标函数值类型
     * @param NP          雇佣蜂数量
     * @param limit       解未改进最大迭代次数
     */
    public ABCIDS(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, String fitnessType, int NP, int limit) {
        super(MAX_GEN, driverList, taskList, 0, fitnessType);
        this.NP = NP;
        this.max_gen = MAX_GEN;
        this.limit = limit;

        fitness = new double[FN];
        f = new double[FN];
        trial = new double[FN];
        prob = new double[FN];
    }

    /**
     * 初始化种群
     */
    @Override
    protected void populationInit() {
        super.init();
        population = new ArrayList<>(FN);
        for (int i = 0; i < FN; i++) {
            init(i);
        }
    }

    /**
     * 初始化第i个食物源
     *
     * @param idx 食物源编号
     */
    private void init(int idx) {
        LayerCode layerCode = genCode();
        f[idx] = layerCode.calFitness();
        fitness[idx] = calculateFitness(f[idx]);

        if (idx < population.size()) {
            population.set(idx, layerCode);
        } else {
            population.add(layerCode);
        }

    }

    @Override
    public SolutionCode solve() {
        //初始化种群
        populationInit();
        //解评价
        evaluate();
        for (int t = 0; t < max_gen; t++) {
            sendEmployedBees();  //引领蜂阶段
            calculateProbabilities();
            sendOnlookerBees();  //跟随蜂阶段
            evaluate();
            sendScoutBees();     //侦查蜂阶段
        }
        return bestS;
    }

    /**
     * 侦查蜂阶段
     * limit 代后还没有改进的食物源直接丢弃，随机产生新的食物源
     */
    private void sendScoutBees() {
        int max_trial_index, i;
        max_trial_index = 0;
        for (i = 1; i < FN; i++) {
            if (trial[i] > trial[max_trial_index])
                max_trial_index = i;
        }
        if (trial[max_trial_index] >= limit) {
            init(max_trial_index);
        }
    }

    /**
     * 跟随蜂阶段
     * 跟随蜂按照一定概率跟随引领蜂
     */
    private void sendOnlookerBees() {
        int i = 0, t = 0;
        while (t < FN) {
            if (random.nextFloat() < prob[i]) {
                t++;
                search(i);
            }
            i++;
            if (i == FN)
                i = 0;
        }
    }


    /**
     * 雇佣蜂(引领蜂)阶段
     * 引领蜂在蜜源邻域内搜索
     */
    private void sendEmployedBees() {
        for (int i = 0; i < FN; i++) {
            search(i);
        }

    }

    // 搜索第i个食物源的邻域
    // Vid = Xid + rand(-1,1)*(Xid - Xjd)
    private void search(int i) {
        LayerCode solutionCode = (LayerCode) population.get(i);
        int toChange = random.nextInt(FN);
        while (toChange == i) {
            toChange = random.nextInt(FN);
        }

        Gene[] path = solutionCode.toGeneArray();
        List<SO> listV = minus(path, ((LayerCode) population.get(toChange)).toGeneArray());
        int v = (int) (listV.size() * random.nextFloat());
        List<SO> vii = new ArrayList<>();
        for (int j = 0; j < v; j++) {
            vii.add(listV.get(j));
        }

        exchange(path, vii);
        LayerCode layerCode = toCode(path);
        if (layerCode.getFitness() < bestF) {
            f[i] = layerCode.getFitness();
            fitness[i] = calculateFitness(f[i]);
            trial[i] = 0;
        } else {
            trial[i]++;
        }
    }

    private LayerCode toCode(Gene[] path) {
        List<VehicleCode> vehicleCodeList = new ArrayList<>();
        Driver driver = DataLab.getDriver(path[0].getCode());
        List<OrderCode> taskList = new ArrayList<>();
        for (int i = 1; i < path.length; i++) {
            if (path[i].getType() == Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                vehicleCodeList.add(new VehicleCode(driver, taskList.size() == 0 ? 0 : 1, taskList));
                driver = DataLab.getDriver(path[i].getCode());
                taskList = new ArrayList<>();
            } else {
                taskList.add(new OrderCode(path[i].getType() - 1, DataLab.getTask(path[i].getCode())));
            }
        }
        vehicleCodeList.add(new VehicleCode(driver, taskList.size() == 0 ? 0 : 1, taskList));
        return new LayerCode(vehicleCodeList, fitnessType);
    }

    /**
     * 执行交换，更新粒子
     *
     * @param path Gene[]
     * @param vii  存储的是需要交换的下标对
     */
    private void exchange(Gene[] path, List<SO> vii) {
        Gene tmp;
        for (SO so : vii) {
            int x = so.getX();
            int y = so.getY();
            tmp = path[x];
            //车<-->车
            if (tmp.getType() == Gene.GENE_TYPE.TYPE_CAR.getCode() && path[y].getType() == Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                //System.out.println("//车<-->车");
                path[so.getX()] = path[so.getY()];
                path[so.getY()] = tmp;

            } else if (tmp.getType() != Gene.GENE_TYPE.TYPE_CAR.getCode() && path[y].getType() != Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                //订单<-->订单
                //System.out.println("订单<-->订单");
                int x1 = findOpp(tmp, path);
                int y1 = findOpp(path[y], path);
                if (tmp.getType() == path[y].getType()) {
                    path[so.getX()] = path[so.getY()];
                    path[so.getY()] = tmp;

                    tmp = path[x1];
                    path[x1] = path[y1];
                    path[y1] = tmp;
                } else {
                    path[so.getX()] = path[y1];
                    path[y1] = tmp;

                    tmp = path[x1];
                    path[x1] = path[so.getY()];
                    path[so.getY()] = tmp;
                }
            } else {  //订单<-->车
                //System.out.println("订单<-->车");
                if (tmp.getType() == Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                    path = reGene(x, y, path);
                } else {//车<-->订单
                    path = reGene(y, x, path);
                }
            }

        }
    }

    /**
     * 修复基因
     *
     * @param x    车辆码索引
     * @param y    订单码索引
     * @param path Gene[]
     */
    private Gene[] reGene(int x, int y, Gene[] path) {
        if (x == 0) {
            x++;
        }
        if (x == y) {
            return path;
        }

        //test
        if (x != y) {
            return path;
        }

        GLink<Gene> gl = new GLink<>(path);
        gl.print();
        int[] yCar = findNearCar(path, y);
        int yc1 = yCar[0];
        int yc2 = yCar[1];
        Gene xGene = path[x]; //车辆基因
        Gene yGene = path[y]; //订单基因
        Gene gc1 = path[yc1]; //替换位置之前车辆基因
        Gene gc2 = path[yc2]; //替换位置之后车辆基因
        Gene y1 = findOppGene(path[y]);
        //交换基因
        gl.exchange(xGene, yGene);
        if (path[y].getType() == Gene.GENE_TYPE.TYPE_ORDER_GET.getCode()) {  //车<-->取货码
            gl.removeNode(y1);                         //删除送货码基因
            gl.addNode(y1, gl.idx(yGene) + 1);    //将送货码至于取货码之后
        } else {//车<-->送货码
            gl.removeNode(y1);
            gl.addNode(y1, gl.idx(yGene) - 1);    //将取货码至于送货码之前
        }


        //car1(yc1)....替换位置(y)....car2(yc2)
        //修复替换位置前的基因
        yc1 = gl.idx(gc1);
        y = gl.idx(xGene);
        for (int i = yc1; i < y; i++) {
            Gene gene = gl.getNode(i).data;
            if (gene.getType() == Gene.GENE_TYPE.TYPE_ORDER_GET.getCode()) {
                Gene opp = findOppGene(gene); //找对应的送货码索引
                if (gl.idx(opp) > y) { //如果送货码索引大于替换位置索引，则将送货码前移到替换位置之前
                    gl.removeNode(opp);
                    gl.addNode(opp, y - 1);
                }
            }

        }
        //修复替换位置后的基因
        yc2 = gl.idx(gc2);
        y = gl.idx(xGene);
        for (int i = y + 1; i <= yc2; i++) {
            Gene gene = gl.getNode(i).data;
            if (gene.getType() == Gene.GENE_TYPE.TYPE_ORDER_PUT.getCode()) {
                Gene opp = findOppGene(gene); //找对应的送货码索引
                if (gl.idx(opp) < y) { //如果取货码索引小于替换位置索引，则将送货码后移到替换位置之后
                    gl.removeNode(opp);
                    gl.addNode(opp, y + 1);

                }
            }

        }
        List<Gene> genes = gl.toArray();
        Gene[] newPath = new Gene[genes.size()];
        for (int i = 0; i < genes.size(); i++) {
            newPath[i] = genes.get(i);
        }
        return newPath;
    }

    private int[] findNearCar(Gene[] path, int index) {
        int p = -1;
        int q = -1;
        int tmp = -1;
        for (int i = 0; i < path.length; i++) {
            Gene gene = path[i];
            if (i == index) {
                p = tmp;
            }

            if (gene.getType() == Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                if (i > index) {
                    q = i;
                    break;
                }

                tmp = i;
            }
        }
        if (q == -1)
            q = path.length - 1;
        return new int[]{p, q};
    }

    private int findOpp(Gene gene, Gene[] path) {
        int index = 0;
        for (Gene g : path) {
            if (g.getCode() == gene.getCode() && g.getType() == 5 - gene.getType()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private Gene findOppGene(Gene gene) {
        if (gene.getType() == Gene.GENE_TYPE.TYPE_ORDER_GET.getCode()) {
            return new Gene(Gene.GENE_TYPE.TYPE_ORDER_PUT.getCode(), gene.getCode());
        } else if (gene.getType() == Gene.GENE_TYPE.TYPE_ORDER_PUT.getCode()) {
            return new Gene(Gene.GENE_TYPE.TYPE_ORDER_GET.getCode(), gene.getCode());
        } else {
            return gene;
        }
    }

    /**
     * 生成交换对，把a变成和b一样，返回需要交换的下标对列表
     */
    private List<SO> minus(Gene[] a, Gene[] b) {
        Gene[] tmp = a.clone();
        ArrayList<SO> list = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < b.length; i++) {
            if (tmp[i] != b[i]) {
                //在tmp中找到和b[i]相等的值，将下标存储起来
                for (int j = i + 1; j < tmp.length; j++) {
                    if (tmp[j] == b[i]) {
                        index = j;
                        break;
                    }
                }
                SO so = new SO(i, index);
                list.add(so);
            }
        }
        return list;
    }

    /**
     * 生成[-1,1]之间均匀分布的随机数
     */
    private double random() {
        return (random.nextDouble() - 0.5) * 2;
    }

    /**
     * 计算选择概率
     */
    private void calculateProbabilities() {
        double max_fit = fitness[0];
        for (int i = 1; i < FN; i++) {
            if (fitness[i] > max_fit)
                max_fit = fitness[i];
        }

        for (int i = 0; i < FN; i++) {
            prob[i] = (0.9 * (fitness[i] / max_fit)) + 0.1;
        }
    }

    /**
     * 种群评价
     */
    @Override
    protected void evaluate() {
        for (int i = 0; i < FN; i++) {
            if (f[i] < bestF && f[i] > 0) {
                bestF = f[i];
                bestS = population.get(i);
            }
        }
    }

    /**
     * 通过目标函数值计算适应度值
     *
     * @param fun 目标函数值
     * @return 适应度
     */
    double calculateFitness(double fun) {
        double result = 0;
        if (fun >= 0) {
            result = 1 / (fun + 1);
        } else {
            result = 1 + Math.abs(fun);
        }
        return result;
    }

    public LayerCode genCode() {
        //乱序
        Collections.shuffle(driverList);
        Collections.shuffle(taskList);

        //将订单按照车辆数切分
        List<Integer> random = IdsUtil.random(driverList.size(), taskList.size());
        List<List<TransportTask>> orders = new ArrayList<>();
        int p = 0;
        for (int k : random) {
            orders.add(taskList.subList(p, p + k));
            p += k;
        }
        List<VehicleCode> vehicleCodeList = new ArrayList<>();
        int i = 0;
        for (Driver driver : driverList) {
            List<OrderCode> codeList = new ArrayList<>();
            List<TransportTask> transportTasks = orders.get(i);
            if (transportTasks != null && transportTasks.size() > 0) {
                List<Integer> shuff = shuff(transportTasks.size());
                Set<Integer> keySet = new HashSet<>();
                for (int k : shuff) {
                    TransportTask order = transportTasks.get(k - 1);
                    if (keySet.contains(k)) {
                        codeList.add(new OrderCode(2, order));
                    } else {
                        codeList.add(new OrderCode(1, order));
                    }
                    keySet.add(k);
                }
            }
            int value = codeList.size() == 0 ? 0 : 1;
            VehicleCode vc = new VehicleCode(driver, value, codeList);
            vehicleCodeList.add(vc);
            i++;
        }
        return new LayerCode(vehicleCodeList, fitnessType);
    }

    private static List<Integer> shuff(int size) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(i + 1);
            list.add(i + 1);
        }
        Collections.shuffle(list);
        return list;
    }

    public static void main(String[] args) {
        String path = FileUtils.getAppPath() + "/src/main/resources/";
        ABCIDS bee = new ABCIDS(100, DataLab.driverList(path + "vehicle.xls"), DataLab.taskList(path + "task.xls"), "distance");
        SolutionCode solve = bee.solve();
        System.out.println(solve.print());
    }
}
