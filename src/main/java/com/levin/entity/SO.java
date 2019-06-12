package com.levin.entity;

import lombok.Data;

@Data
public class SO {
    private int x;
    private int y;

    public SO(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
