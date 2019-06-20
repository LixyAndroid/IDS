package com.levin.core.entity.code;

import com.levin.core.entity.fitness.Fitness;
import com.levin.core.entity.fitness.FitnessFactory;

import java.util.*;

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
     * 禁忌搜索，寻找最优子结构组成的最优领域
     */
    public SolutionCode bestNeighbor(int NN) {
        Set<VehicleCode> tabuList = new HashSet<>();
        List<VehicleCode> vehicleCodes = new ArrayList<>();
        for (VehicleCode vehicleCode : this.vehicleCodeList) {
            double bestF = Double.MAX_VALUE;
            VehicleCode bestV = vehicleCode;
            // 每辆车的订单不变，只改变了其装、送顺序。达到局部最优
            VehicleCode neighbor = new VehicleCode(vehicleCode.getDriver(), vehicleCode.getValue(),
                    shuff(vehicleCode.getOrderCodeList()));
            neighbor.calFitness(fitnessType);
            for (int i = 0; i < NN; i++) {
                if (vehicleCode.getOrderCodeList() == null || vehicleCode.getOrderCodeList().size() <= 2) {
                    break;
                }
                if (!tabuList.contains(neighbor)) {
                    if (neighbor.getFitness() < bestF && neighbor.getFitness() > 0) {
                        bestF = neighbor.getFitness();
                        bestV = neighbor;
                    }
                }
                tabuList.add(neighbor);
                neighbor = new VehicleCode(neighbor.getDriver(), neighbor.getValue(),
                        shuff(neighbor.getOrderCodeList()));
                neighbor.calFitness(fitnessType);
            }
            vehicleCodes.add(bestV);
        }
        return new TreeCode(vehicleCodes, fitnessType);
    }


    /**
     * 重排装箱顺序
     *
     * @param orderCodeList 装箱顺序集合
     */
    private List<OrderCode> shuff(List<OrderCode> orderCodeList) {
        if (orderCodeList == null) {
            return null;
        }
        Set<String> tmp = new HashSet<>();
        Collections.shuffle(orderCodeList);
        List<OrderCode> codeList = new ArrayList<>();
        for (OrderCode oc : orderCodeList) {
            if (!tmp.contains(oc.getTask().getId())) {
                codeList.add(new OrderCode(1, oc.getTask()));
            } else {
                codeList.add(new OrderCode(2, oc.getTask()));
            }
            tmp.add(oc.getTask().getId());
        }
        return codeList;
    }

    /**
     * 打印路径
     */
    public abstract String print();

    public void setDd(double dd) {
        this.dd = dd;
    }

    public double getFitness() {
        if (fitness == 0)
            return calFitness();
        return fitness;
    }

    public void setFitnessType(Fitness fitnessType) {
        this.fitnessType = fitnessType;
    }

    public void setVehicleCodeList(List<VehicleCode> vehicleCodeList) {
        this.vehicleCodeList = vehicleCodeList;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public void setCarNumUsed(int carNumUsed) {
        this.carNumUsed = carNumUsed;
    }

    public double getDd() {
        return dd;
    }

    public abstract SolutionCode repair();
}
