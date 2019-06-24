package com.levin.web;

import lombok.Data;

@Data
public class AlgoPara {
    private int maxGen = 1;
    private int size = 50;
    private int n = 100;
    private String fitnessType = "distance";
    private int alpha = 200;
    private int beta = 50;
    private int gama = 50;
    private int b1 = 20;
    private int b2 = 30;
}
