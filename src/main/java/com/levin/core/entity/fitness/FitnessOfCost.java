package com.levin.core.entity.fitness;

import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;

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
        return 0;
    }
}
