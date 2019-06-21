package com.levin.core.algo;

import com.levin.core.entity.code.LayerCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.clustring.ParticleClustering;
import com.levin.entity.SO;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.core.entity.code.LayerCodeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 粒子群算法求解VRP
 */
public class PsoIDS extends IDS {
    private List<LayerCode> mUnits = new ArrayList<>();   //粒子群
    private static final float w = 0.5f;    //惯性权重

    private HashMap<Integer, LayerCode> Pd = new HashMap<>();    //一颗粒子历代中出现最好的解
    private LayerCode Pgd;   // 整个粒子群经历过的的最好的解，每个粒子都能记住自己搜索到的最好解
    private List<LayerCode> Pgds = new ArrayList<>();
    private LayerCode Md;    // 突变解

    private ArrayList<ArrayList<SO>> listV = new ArrayList<>();    //自身交换序列，即惯性因子


    /**
     * 初始温度
     */
    private float T0 = 5000f;

    /**
     * 当前温度
     */
    private float T = T0;

    /**
     * 退温系数
     */
    private float alpha = 0.9f;

    public PsoIDS(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList, int size, String fitnessType) {
        super(MAX_GEN, driverList, taskList, size, fitnessType);
    }

    @Override
    protected void populationInit() {

    }

    @Override
    protected void evaluate() {

    }

    /**
     * 初始化参数配置
     */
    public void init() {
        super.init();
        size = driverList.size() + taskList.size() * 2;
    }


    /**
     * 初始化种群
     */
    public void initGroup() {
        //产生备选集合
        for (int i = 0; i < size; i++) {
            for (int k = 0; k < size; k++) {
                population.add(LayerCodeUtil.genCode(driverList, taskList, fitnessType));
            }
        }
        clustring();
    }

    private void clustring() {
        //聚类生成初始解集
        List<LayerCode> layerCodes = new ArrayList<>();
        for (SolutionCode sc : mUnits) {
            layerCodes.add((LayerCode) sc);
        }

        ParticleClustering particleClustering = new ParticleClustering(layerCodes, size, 10);
        particleClustering.clustering();
        mUnits = particleClustering.getClusteringCenterT();
    }


    /**
     * 初始化自身的交换序列即惯性因子
     */
    private void initListV() {
        for (int i = 0; i < size; i++) {
            ArrayList<SO> list = new ArrayList<>();
            int n = random.nextInt(size - 1) % (size);    //随机生成一个数，表示当前粒子需要交换的对数
            for (int j = 0; j < n; j++) {
                //生成两个不相等的编号x,y
                int x = random.nextInt(size - 1) % (size);
                int y = random.nextInt(size - 1) % (size);
                while (x == y) {
                    y = random.nextInt(size - 1) % (size);
                }

                //x不等于y
                SO so = new SO(x, y);
                list.add(so);
            }
            listV.add(list);
        }
    }

    public SolutionCode solve() {
        initGroup();

        initListV();

        //挑选最好的个体
        for (int i = 0; i < size; i++) {
            Pd.put(i, mUnits.get(i));
        }
        Pgd = Pd.get(0);
        for (int i = 0; i < size; i++) {
            if (Pgd.getFitness() > Pd.get(i).getFitness()) {
                Pgd = Pd.get(i);
            }
        }

        // 进化
        evolution();

        return bestS;

    }

    /**
     * 判断是否收敛
     *
     * @return
     */
    public boolean isconvergence() {
        int s = Pgds.size();
        if (s < MAX_GEN / 100) {
            return false;
        }

        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < i; j++) {
                if (Pgds.get(s - i).getFitness() != Pgds.get(s - j).getFitness()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void mutation() {
        double ratios = 0;
        while (ratios == 0 || ratios > 0.5) {
            LayerCode layerCode = LayerCodeUtil.genCode(driverList, taskList, fitnessType);
            for (LayerCode p : mUnits) {
                ratios += LayerCodeUtil.ratio(Md.toString(), p.toString());
            }
            ratios = ratios / mUnits.size();
        }
    }

    /**
     * 进化
     */
    private void evolution() {
        for (int t = 0; t < MAX_GEN; t++) {
            //如果收敛，产生突变
            if (isconvergence()) {
                mutation();
                Pgds = new ArrayList<>();
            }
            for (int k = 0; k < size; k++) {
                ArrayList<SO> vii = new ArrayList<>();
                //更新公式：Vii=wVi+ra(Pid-Xid)+rb(Pgd-Xid)
                //第一部分，自身交换对
                int len = (int) (w * listV.get(k).size());
                for (int i = 0; i < len; i++) {
                    vii.add(listV.get(k).get(i));
                }

                //第二部分，和当前粒子中出现最好的结果比较，得出交换序列
                //ra(Pid-Xid)
                ArrayList<SO> a = LayerCodeUtil.minus(mUnits.get(k).toGeneArray(), Pd.get(k).toGeneArray());
                float ra = random.nextFloat();
                len = (int) (ra * a.size());
                for (int i = 0; i < len; i++) {
                    vii.add(a.get(i));
                }

                //第三部分，和全局最优的结果比较，得出交换序列
                //rb(Pgd-Xid)
                ArrayList<SO> b = LayerCodeUtil.minus(mUnits.get(k).toGeneArray(), Md == null ? Pgd.toGeneArray() : Md.toGeneArray());
                float rb = random.nextFloat();
                len = (int) (rb * b.size());
                for (int i = 0; i < len; i++) {
                    vii.add(b.get(i));
                }

                listV.remove(0);    //移除当前，加入新的
                listV.add(vii);

                //执行交换，生成下一个粒子
                LayerCodeUtil.exchange(mUnits.get(k).toGeneArray(), vii);
                mUnits.get(k).calFitness();

                //加入模拟退火操作
                sa(k);
            }

            //更新适应度的值，并挑选最好的个体
            for (int i = 0; i < size; i++) {
                if (Pgd.getFitness() > Pd.get(i).getFitness()) {
                    Pgd = Pd.get(i);
                    Md = null;
                }
            }

            //保存每一代的最优解
            Pgds.add(Pgd);

            //退温操作
            T = alpha * T;
        }
    }

    /**
     * 模拟退火操作
     */
    public void sa(int k) {
        //更新适应度函数
        mUnits.get(k).calFitness();
        if (Pd.get(k).getFitness() > mUnits.get(k).getFitness()) {
            Pd.put(k, mUnits.get(k));
        }

        //随机产生一个新粒子
        LayerCode rp = LayerCodeUtil.genCode(driverList, taskList, fitnessType);
        LayerCode paticle = Pd.get(k);
        if (paticle.getFitness() > rp.getFitness()) {
            Pd.put(k, rp);
        } else {
            double detaC = Math.abs(rp.getFitness() - paticle.getFitness());
            if (random.nextFloat() < Math.min(1, Math.pow(Math.E, -detaC / T))) {
                Pd.put(k, rp);
            }
        }
    }

    public static void main(String[] args) {
    }
}
