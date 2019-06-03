package com.levin.core.entity.fitness;

import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;

import java.util.List;

public class FitnessOfProfit implements Fitness {
    private FitnessOfCost foc = new FitnessOfCost();

    @Override
    public double calculate(SolutionCode code) {
        double fitness = 0;
        for (VehicleCode vc : code.vehicleCodeList) {
            if (vc.calFitness(this) < 0) {
                vc.print();
                return -1;
            }

            if (vc.getOrderCodeList() == null || vc.getValue() == 0)
                continue;

            fitness += vc.calFitness(this);
        }
        return fitness;
    }

    @Override
    public double calculate(VehicleCode code) {
        List<OrderCode> orderCodeList = code.getOrderCodeList();
        double profitSum = 0;
        for (OrderCode orderCode : orderCodeList) {
            if (orderCode.getType() == 1) {
                profitSum += orderCode.getTask().getAmount();
            }
        }
        return profitSum - foc.calculate(code);
    }
}
