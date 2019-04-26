package com.levin.core.entity.fitness;

/**
 * 目标函数简单工厂
 */
public class FitnessFactory {
    public static Fitness get(String type) {
        if (type.equalsIgnoreCase("cost")) {
            return new FitnessOfCost();
        } else if (type.equalsIgnoreCase("time")) {
            return new FitnessOfTime();
        } else if (type.equalsIgnoreCase("distance")) {
            return new FitnessOfDistance();
        }
        return null;
    }
}
