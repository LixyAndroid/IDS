package com.levin.core.entity.fitness;

import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.entity.CarProp;
import com.levin.entity.CarPropLab;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 成本最小模型
 */
public class FitnessOfCost implements Fitness {
    private static final FitnessOfTime fot = new FitnessOfTime();

    @Override
    public double calculate(SolutionCode code) {
        double fitness = 0;
        for (VehicleCode vc : code.vehicleCodeList) {
            if (vc.calFitness(this) < 0) {
                return -1;
            }

            if (vc.getValue() == 2) //虚拟车辆
                continue;
            fitness += vc.calFitness(this);
        }
        return fitness;
    }

    @Override
    public double calculate(VehicleCode code) {
        Driver driver = code.getDriver();
        List<OrderCode> orderCodeList = code.getOrderCodeList();

        if (orderCodeList == null || orderCodeList.size() == 0) {
            return 0;
        }

        //车辆参数
        CarProp carProp = CarPropLab.get(driver.getType());
        int idx = DataLab.getIdx(driver.getId(), 0);
        double cost = carProp.getC();    //固定成本
        double weight = 0d;

        //运输成本
        for (OrderCode oc : orderCodeList) {
            TransportTask task = oc.getTask();
            int current = DataLab.getIdx(task.getId(), oc.getType());
            double distance = DataLab.getDistance(idx, current);
            if (weight > carProp.getG()) {//超载
                return -1;
            }

            if (weight < carProp.getG()) { //空载
                cost += distance * carProp.getNC();
            } else {  //满载
                cost += distance * carProp.getFC();
            }

            if (oc.getType() == OrderCode.TYPE.Pick.getCode()) {
                weight += task.getPlatenNum();
            } else {
                weight -= task.getPlatenNum();
            }

            idx = current;
        }

        // 超时惩罚成本
        List<OrderCode> orderCodes = new ArrayList<>();
        for (OrderCode orderCode : orderCodeList) {
            if (orderCode.getType() == OrderCode.TYPE.Pick.getCode()) {
                orderCodes.add(orderCode);
            }
        }

        for (OrderCode oc : orderCodes) {
            double distance = 0;
            String id = code.getDriver().getId();
            for (OrderCode oc2 : orderCodeList) {
                distance += DataLab.getDistance(DataLab.getIdx(id, 0), DataLab.getIdx(oc2.getTask().getId(), oc2.getType()));
                id = oc2.getTask().getId();
                if (oc2.getTask().getId().equalsIgnoreCase(oc.getTask().getId()) && oc2.getType() == OrderCode.TYPE.Deliver.getCode())
                    break;
            }

            double time = oc.getTask().getTw() - oc.getTask().getPlatenDate() - distance / CarPropLab.get(code.getDriver().getType()).getSpeed();
            if (time < 0)
                cost += punishCost(time);
        }

        //车辆滞留成本
        if (code.getValue() == 0) {
            Integer day = code.getDriver().getRetention();//滞留天数
            cost += retentionCost(day);
        }

        return cost;
    }

    /**
     * 订单超时惩罚成本
     */
    private static double punishCost(double time) {
        if (time > 10)
            return 1000;
        return 100 * Math.abs(time);
    }

    /**
     * 车辆滞留成本
     */
    private static double retentionCost(double day) {
        double x = (day + 1) / DataLab.getAvgTimeOfCar();
        return 100 * Math.pow(x - 1, Math.E);
    }
}
