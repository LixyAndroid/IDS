package com.levin.core.entity.fitness;

import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.entity.CarPropLab;
import com.levin.excel.DataLab;

import java.util.List;

/**
 * 距离最短模型
 */
public class FitnessOfDistance implements Fitness {

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
        double z = 0;
        double sum = 0;
        List<OrderCode> orderCodeList = code.getOrderCodeList();
        if (orderCodeList != null && orderCodeList.size() > 0) {
            int idx = DataLab.getIdx(code.getDriver().getId(), 0);
            for (OrderCode orderCode : orderCodeList) {
                int current = DataLab.getIdx(orderCode.getTask().getId(), orderCode.getType());
                z += DataLab.getDistance(idx, current);
                idx = current;

                double g = orderCode.getTask().getPlatenNum();
                if (orderCode.getType() == 1) {
                    sum += g;
                } else {
                    sum -= g;
                }
                if (sum < 0 || sum > CarPropLab.get(code.getDriver().getType()).getG()) {
                    return -1;
                }
            }
        }
        return z;
    }
}
