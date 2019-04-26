package com.levin.entity;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * 车辆属性集合
 */
public class CarPropLab {
    private static final Map<String, CarProp> propMap = ImmutableMap.of(
            "L42", new CarProp(2, 14, 198, 0.853, 1.218, 50),
            "L62", new CarProp(4, 30, 223, 1.214, 1.734, 50),
            "L96", new CarProp(12, 53, 544, 2.126, 3.037, 50),
            "L56", new CarProp(8, 8, 544, 2.126, 3.037, 50),
            "L175", new CarProp(25, 125, 885, 3.682, 5.260, 50)
    );

    public static CarProp get(String type) {
        return propMap.getOrDefault(type, new CarProp(8, 8, 200, 2.5, 3.6, 50));
    }
}
