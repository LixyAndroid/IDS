package com.levin.core.entity.code;

import com.levin.core.entity.fitness.Fitness;
import com.levin.core.entity.fitness.FitnessFactory;

import java.util.List;

/**
 * 解的编码
 */
public abstract class SolutionCode {
    protected Fitness fitnessType;
    /**
     * 车辆分配方案
     */
    public List<VehicleCode> vehicleCodeList;
    /**
     * 目标函数值
     */
    protected double fitness;
    /**
     * 多样性距离
     */
    protected double dd;

    /**
     * 使用的车辆数目
     */
    protected int carNumUsed;

    public SolutionCode() {
    }

    public SolutionCode(List<VehicleCode> vehicleCodeList, String fitnessType) {
        this.vehicleCodeList = vehicleCodeList;
        this.fitnessType = FitnessFactory.get(fitnessType);
        calCarNumUsed();
        calFitness();
    }

    public SolutionCode(List<VehicleCode> vehicleCodeList, Fitness fitnessType) {
        this.vehicleCodeList = vehicleCodeList;
        this.fitnessType = fitnessType;
        calCarNumUsed();
        calFitness();
    }

    /**
     * 计算车辆使用数目
     */
    private void calCarNumUsed() {
        for (VehicleCode vc : vehicleCodeList) {
            if (vc.getOrderCodeList() != null
                    && vc.getOrderCodeList().size() > 0)
                carNumUsed++;

        }
    }

    /**
     * 计算目标函数值
     */
    public abstract double calFitness();

    /**
     * 获取该装载方案下的最优行车路线
     */
    public abstract SolutionCode bestSolution(int NN);

    /**
     * 打印路径
     */
    public abstract String print();

    public void setDd(double dd) {
        this.dd = dd;
    }

    public double getFitness() {
        return fitness;
    }

    public double getDd() {
        return dd;
    }

    public abstract SolutionCode repair();
}
