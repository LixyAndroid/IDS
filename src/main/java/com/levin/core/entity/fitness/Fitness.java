package com.levin.core.entity.fitness;

import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;

/**
 * 目标函数
 */
public interface Fitness {

    double calculate(SolutionCode code);

    double calculate(VehicleCode code);
}
