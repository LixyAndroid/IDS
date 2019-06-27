package com.levin.core.algo;

import com.levin.core.entity.code.LayerCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.entity.Gene;
import com.levin.entity.SO;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.util.FileUtils;
import com.levin.core.entity.code.LayerCodeUtil;

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
        List<SO> listV = LayerCodeUtil.minus(path, ((LayerCode) population.get(toChange)).toGeneArray());
        int v = (int) (listV.size() * random.nextFloat());
        List<SO> vii = new ArrayList<>();
        for (int j = 0; j < v; j++) {
            vii.add(listV.get(j));
        }

        LayerCodeUtil.exchange(path, vii);
        LayerCode layerCode = LayerCodeUtil.toCode(path,fitnessType);
        if (layerCode.getFitness() < bestF) {
            f[i] = layerCode.getFitness();
            fitness[i] = calculateFitness(f[i]);
            trial[i] = 0;
        } else {
            trial[i]++;
        }
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
        return LayerCodeUtil.genCode(driverList, taskList, fitnessType);
    }


    public static void main(String[] args) {
        String path = FileUtils.getAppPath() + "/src/main/resources/";
        ABCIDS bee = new ABCIDS(100, DataLab.driverList(path + "vehicle.xls"), DataLab.taskList(path + "task.xls",-1), "distance");
        SolutionCode solve = bee.solve();
        System.out.println(solve.print());
    }
}
