package com.levin.core.entity.code;


import com.levin.core.entity.fitness.Fitness;
import com.levin.excel.Driver;
import lombok.Data;

import java.util.*;

/**
 * 车辆订单分配（分配某一辆车运输的订单）
 */
@Data
public class VehicleCode {
    /**
     * 车辆是否分配任务（1：分配，0：不分配）
     */
    private int value;

    /**
     * 车辆
     */
    private Driver driver;

    /**
     * 订单装载方案
     */
    private List<OrderCode> orderCodeList;

    /**
     * 目标函数值
     */
    private double fitness;

    public VehicleCode(Driver driver, int value, List<OrderCode> orderCodeList) {
        this.driver = driver;
        this.value = value;
        this.orderCodeList = orderCodeList;
    }

    public VehicleCode neighbor() {
        if (orderCodeList == null) {
            return null;
        }

        Set<String> tmp = new HashSet<>();
        Collections.shuffle(orderCodeList);
        List<OrderCode> codeList = new ArrayList<>();
        for (OrderCode oc : orderCodeList) {
            if (tmp.contains(oc.getTask().getId())) {
                codeList.add(new OrderCode(2, oc.getTask()));
            } else {
                codeList.add(new OrderCode(1, oc.getTask()));
            }
            tmp.add(oc.getTask().getId());
        }
        return new VehicleCode(driver, value, codeList);
    }

    public double calFitness(Fitness fitness) {
        if (this.fitness > 0) {
            return this.fitness;
        }

        this.fitness = fitness.calculate(this);
        return this.fitness;
    }

    public VehicleCode bestNeighbor(Fitness fitness) {
        int n = orderCodeList.size();
        double min = Integer.MAX_VALUE;
        VehicleCode best = this;
        Set<VehicleCode> tabuList = new HashSet<>();
        for (int i = 0; i < Math.pow(2, n); i++) {
            VehicleCode neighbor = neighbor();
            if (!tabuList.contains(neighbor) && neighbor.calFitness(fitness) < min) {
                best = neighbor;
                min = neighbor.calFitness(fitness);
            }
            tabuList.add(neighbor);
        }
        return best;
    }
}
