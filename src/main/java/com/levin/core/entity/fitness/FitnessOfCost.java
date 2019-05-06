package com.levin.core.entity.fitness;

import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.entity.CarProp;
import com.levin.entity.CarPropLab;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;

import java.util.List;

/**
 * 成本最小模型
 */
public class FitnessOfCost implements Fitness {
    @Override
    public double calculate(SolutionCode code) {
        double fitness = 0;
        for (VehicleCode vc : code.vehicleCodeList) {
            if (vc.calFitness(this) < 0) {
                return -1;
            }
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
        return cost;
    }
}
