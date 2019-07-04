package com.levin.core.algo;

import com.levin.core.entity.code.LayerCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.entity.Gene;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.core.entity.code.LayerCodeUtil;
import com.levin.web.AlgoPara;
import org.apache.commons.lang3.ArrayUtils;

import java.text.DecimalFormat;
import java.util.*;

/**
 * 遗传算法
 */
public class GaIDS extends IDS {
    /**
     * 父代种群
     */
    private List<LayerCode> oldPopulation = new ArrayList<>();
    private List<LayerCode> tmpPopulation = new ArrayList<>();

    /**
     * 子代种群
     */
    private List<LayerCode> newPopulation = new ArrayList<>();

    /**
     * 种群中各个个体的累计概率
     */
    private double[] Pi;

    /**
     * 每次搜索邻居个数
     */
    private int N;

    /**
     * 禁忌表
     */
    private Set<LayerCode> tabuList = new HashSet<>();

    /**
     * 当前代数
     */
    private int t = 0;

    /**
     * 最佳出现代数
     */
    private int bestT = 0;

    /**
     * 种群最大适应度
     */
    private double Fmax = 0;

    /**
     * 种群平均适应度
     */
    private double Favg = 0;

    /**
     * 种群适应度之和
     */
    private double Fsum = 0;

    private int ts;

    private DecimalFormat df = new DecimalFormat("#.00");

    public GaIDS(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, int size, String fitnessType, int n) {
        super(MAX_GEN, driverList, taskList, size, fitnessType);
        Pi = new double[size];
        this.N = n;
        ts = driverList.size() + taskList.size();
        super.init();
    }

    @Override
    protected void populationInit() {
        for (int i = 0; i < size; i++) {
            oldPopulation.add(LayerCodeUtil.genCode(driverList, taskList, fitnessType));
        }
    }

    @Override
    protected void evaluate() {
       super.printFit();
        for (LayerCode ch : oldPopulation) {
            if (bestF == 0) {
                bestF = ch.getFitness();
                bestS = ch;
            }
            if (ch.getFitness() < bestF) {
                bestS = ch;
                bestF = ch.getFitness();
                bestT = t;
            }
        }
    }

    /**
     * 赌轮选择策略挑选
     **/
    public LayerCode select() {
        int i = 0;
        float ran = (float) (random.nextInt(65535) % 1000 / 1000.0);
        while (true) {
            for (i = 0; i < size; i++) {
                if (ran <= Pi[i]) {
                    break;
                }
            }

            if (i != size) {
                break;
            }
        }

        return oldPopulation.get(i);
    }

    /**
     * Tabu局部搜索
     *
     * @param ch 起始染色体
     */
    private void tabu(LayerCode ch) {
        LayerCode neighbor = (LayerCode) ch.bestNeighbor(1, 0);
        tabuList = new HashSet<>();
        for (int tt = 0; tt < N; tt++) {
            if (!tabuList.contains(neighbor)) {
                if (neighbor.getFitness() > bestS.getFitness()) {
                    bestS = neighbor;
                    bestF = neighbor.getFitness();
                }
                tabuList.add((LayerCode) neighbor);
            }
            neighbor = (LayerCode) neighbor.bestNeighbor(1, 0);
        }
        //将最好结果放回种群，加快搜索速度
        LayerCode bestC = tabuList.iterator().next();
        if (bestC.getFitness() > ch.getFitness()) {
            replace(ch, bestC);
        }

    }

    /**
     * 替换种群中的个体
     *
     * @param o 原始个体
     * @param n 新个体
     */
    public void replace(LayerCode o, LayerCode n) {
        for (int i = 0; i < size; i++) {
            if (oldPopulation.get(i).equals(o)) {
                tmpPopulation.set(i, n);
            }
        }
    }

    /**
     * OX交叉算子
     *
     * @param ch1 染色体1
     * @param ch2 染色体2
     */
    private void cross(LayerCode ch1, LayerCode ch2) {
        Gene[] temp1 = removeDcode(ch1);
        Gene[] temp2 = removeDcode(ch2);

        int i = random.nextInt(ts - 1);
        int j = i + random.nextInt(ts - i - 1);

        int d = j - i + 1;
        while (d < 2) { //保证交叉基因位大于等于2个
            i = random.nextInt(ts - 1);
            j = i + random.nextInt(ts - i - 1);
            d = j - i + 1;
        }

        Gene[] toChange = new Gene[d];
        Gene[] remain1 = new Gene[ts - d];
        Gene[] remain2 = new Gene[ts - d];

        int r = 0, s = 0;
        for (int k = 0; k < ts; k++) {
            Gene g = temp1[k];
            if (k < i || k > j) {
                remain1[r++] = g;
            } else {
                toChange[s++] = g;
            }
        }

        int q = 0;
        for (int k = 0; k < ts; k++) {
            if (!ArrayUtils.contains(toChange, temp2[k])) {
                remain2[q] = temp2[k];
                temp2[k] = remain1[q++];
            }
        }

        int p1 = 0;
        int p2 = 0;
        for (int k = 0; k < ts; k++) {
            if (k < i || k > j) {
                temp1[p1] = remain2[p2++];
            }
            p1++;
        }

        newPopulation.add(newCh(temp1));
        newPopulation.add(newCh(temp2));
    }

    /**
     * 变异算子
     *
     * @param ch 变异染色体
     */
    private void variation(LayerCode ch) {
        Gene[] temp = removeDcode(ch);

        //生成交换位置
        int r = random.nextInt(ts - 1);
        int s = random.nextInt(ts - 1);

        while (r == s) {
            s = random.nextInt(ts - 1);
        }


        Gene gr = temp[r];
        Gene gs = temp[s];

        temp[r] = gs;
        temp[s] = gr;

        newPopulation.add(newCh(temp));
    }

    /**
     * 去除送货码基因
     */
    private Gene[] removeDcode(LayerCode ch) {
        Gene[] path = ch.toGeneArray();
        Gene[] temp = new Gene[ts];

        //将送货码基因去除
        int i = 0;
        for (Gene g : path) {
            if (g.getType() != 3) {  //车辆码+取货码
                temp[i++] = g;
            }
        }
        return temp;
    }

    /**
     * 增加送货码
     *
     * @param temp 车辆码+取货码
     */
    private LayerCode newCh(Gene[] temp) {
        //保证第一个基因位为车辆码
        if (temp[0].getType() != 1) {
            for (int k = 0; k < ts; k++) {
                Gene g = temp[k];
                if (g.getType() == 1) {
                    Gene first = temp[0];
                    temp[0] = g;
                    temp[k] = first;
                    break;
                }
            }
        }

        //增加送货码
        ArrayList<Gene> newPathList = new ArrayList<Gene>();
        //Gene[] newPath = new Gene[size];
        //int p = 0;
        for (Gene g : temp) {
            //newPath[p++] = g;
            newPathList.add(g);
            if (g.getType() == 2) {
                //newPath[p++] = new Gene(3, g.getCode());
                newPathList.add(new Gene(3, g.getCode()));
            }
        }
        Gene[] newPath = newPathList.toArray(new Gene[newPathList.size()]);
        return LayerCodeUtil.toCode(newPath, fitnessType);
    }

    /**
     * GA全局搜索
     */
    public void ga() {
        //精英保留
        newPopulation.add((LayerCode) bestS);
        int ps = 1;
        while (ps < size) {
            LayerCode select = select();
            float r = random.nextFloat();// /产生概率
            if (r < calPm(Fmax, Favg, select.getFitness())) {
                //变异
                variation(select);
                ps += 1;
            }

            if (r < calPc(Fmax, Favg, select.getFitness())) {
                //交叉
                LayerCode select2 = select();
                while (select.equals(select2)) {
                    select2 = select();
                }
                cross(select, select2);
                ps += 2;
            }
        }
    }

    public void copy() {
        for (LayerCode ch : oldPopulation) {
            tmpPopulation.add(ch);
        }
    }

    public void evolution() {
        //禁忌搜索
        copy();
        for (LayerCode ch : oldPopulation) {
            float rand = random.nextFloat();
            if (rand < ch.getFitness() / Favg) {
                tabu(ch);
            }
        }

        oldPopulation = tmpPopulation;
        tmpPopulation = new ArrayList<>();

        //遗传算法
        ga();
        oldPopulation = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            oldPopulation.add(newPopulation.get(i));
        }
        newPopulation = new ArrayList<>();
        evaluate();
    }

    /**
     * 统计适应度
     */
    private void calPara() {
        Favg = 0;
        Fmax = 0;
        Fsum = 0;

        for (LayerCode ch : oldPopulation) {
            double f = ch.getFitness();
            if (f > Fmax) {
                Fmax = f;
            }
            Fsum += f;
        }
        Favg = Fsum / size;

        double tempF = 0;
        for (int k = 0; k < size; k++) {
            LayerCode ch = oldPopulation.get(k);
            tempF += ch.getFitness();
            Pi[k] = tempF / Fsum;
        }
    }

    @Override
    public SolutionCode solve() {
        //种群初始化
        populationInit();
        //找出当前种群最优解
        evaluate();

        while (t < MAX_GEN) {
            //计算参数
            calPara();

            //进化
            evolution();

            t++;

        }
        if (fitnessType.equalsIgnoreCase("profit")) {
            bestS.setFitness(1 / bestF * 100000);
        }
        return bestS;
    }

    /**
     * 计算Pc、Pm参数
     */
    private static final double k1 = 1.0;
    private static final double k3 = 1.0;
    private static final double k2 = 0.5;
    private static final double k4 = 0.5;

    /**
     * 变异概率
     *
     * @param Fmax 最大适应度
     * @param Favg 平均适应度
     * @param f    变异染色体适应度
     * @return
     */
    public static double calPm(double Fmax, double Favg, double f) {
        double Pm = 0;
        if (f > Favg) {
            Pm = k2 * (Fmax - f) / (Fmax - Favg);
        } else {
            Pm = k4;
        }
        return Pm;
    }

    /**
     * 交叉概率
     *
     * @param Fmax 最大适应度
     * @param Favg 平均适应度
     * @param f    交叉的两个染色体较大适应度
     * @return
     */
    public static double calPc(double Fmax, double Favg, double f) {
        double Pc = 0;
        if (f > Favg) {
            Pc = k1 * (Fmax - f) / (Fmax - Favg);
        } else {
            Pc = k3;
        }
        return Pc;
    }

    public static void main(String[] args) {
        SolutionCode ga = SolverFactory.solve("ga", 30, new AlgoPara());
        System.out.println(ga.getFitness());
    }
}
