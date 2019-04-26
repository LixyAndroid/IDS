package com.levin.core.entity.code;

import com.levin.core.entity.fitness.Fitness;

/**
 * 双链多层编码
 */
public class LayerCode extends SolutionCode {


    @Override
    public double calFitness() {
        return fitnessType.calculate(this);
    }

    @Override
    public SolutionCode bestSolution(int NN) {
        return null;
    }

    @Override
    public String print() {
        return null;
    }

    @Override
    public SolutionCode repair() {
        return null;
    }
}
