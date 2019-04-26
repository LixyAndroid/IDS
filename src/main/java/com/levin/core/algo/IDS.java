package com.levin.core.algo;

import com.levin.core.entity.code.SolutionCode;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;

import java.util.List;
import java.util.Random;

/**
 * 智能车辆调度算法
 */
public abstract class IDS {
    /**
     * 最大迭代次数
     */
    protected int MAX_GEN;
    /**
     * 司机集合
     */
    protected List<Driver> driverList;
    /**
     * 任务集合
     */
    protected List<TransportTask> taskList;

    /**
     * 距离矩阵
     */
    public static double[][] distance;

    /**
     * 初始种群
     */
    protected List<SolutionCode> population;

    /**
     * 初始种群大小
     */
    protected int size;

    /**
     * 随机数种子
     */
    protected Random random;

    /**
     * 最优目标函数值
     */
    protected double bestF = Double.MAX_VALUE;

    /**
     * 最优编码
     */
    protected SolutionCode bestS;

    protected String fitnessType;

    public IDS() {

    }

    public IDS(int MAX_GEN, List<Driver> driverList, List<TransportTask> taskList,int size,String fitnessType) {
        this.MAX_GEN = MAX_GEN;
        this.driverList = driverList;
        this.taskList = taskList;
        this.size = size;
        this.fitnessType = fitnessType;
    }

    /**
     * 初始化参数
     */
    protected void init() {
        random = new Random(System.currentTimeMillis());
        distance = DataLab.genDisMatrix();
    }


    /**
     * 种群初始化
     */
    protected abstract void populationInit();

    /**
     * 迭代
     *
     * @return 历史最优解
     */
    public abstract SolutionCode solve();

    /**
     * 解的评价
     */
    protected abstract void evaluate();
}
