package com.levin.web;

import com.levin.excel.Driver;
import lombok.Data;

import java.util.List;

@Data
public class Route {
    private Driver driver;
    private List<RouteNode> nodeList;

    public Route() {
    }

    public Route(Driver driver, List<RouteNode> nodeList) {
        this.driver = driver;
        this.nodeList = nodeList;
    }
}
