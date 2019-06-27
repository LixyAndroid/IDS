package com.levin.web;

import lombok.Data;

@Data
public class AlgoPara {
    private String algo = "ss";
    private Integer orderNum = -1;
    private Integer maxGen = 1;
    private Integer size = 50;
    private Integer n = 100;
    private String fitnessType = "distance";
    private Integer alpha = 200;
    private Integer beta = 50;
    private Integer gama = 50;
    private Integer b1 = 20;
    private Integer b2 = 30;
}
