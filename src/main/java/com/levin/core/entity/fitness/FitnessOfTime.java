package com.levin.core.entity.fitness;

import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.entity.CarPropLab;
import com.levin.excel.Driver;

import java.util.List;

/**
 * 压板时间最小模型
 */
public class FitnessOfTime implements Fitness {
    private static final FitnessOfDistance fod = new FitnessOfDistance();

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
        double speed = CarPropLab.get(driver.getType()).getSpeed();
        return fod.calculate(code) / speed;  // 时间 = 路程 / 速度
    }
}
