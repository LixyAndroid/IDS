package com.levin.web;

import lombok.Data;

import java.util.List;

@Data
public class Result {
    private List<Route> routeList;
    private double fitness;
    private double time;
}
