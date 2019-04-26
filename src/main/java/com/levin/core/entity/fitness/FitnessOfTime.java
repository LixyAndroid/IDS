package com.levin.core.entity.fitness;

import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;

/**
 * 滞留时间最小模型
 */
public class FitnessOfTime implements Fitness {
    @Override
    public double calculate(SolutionCode code) {
        return 0;
    }

    @Override
    public double calculate(VehicleCode code) {
        return 0;
    }
}
