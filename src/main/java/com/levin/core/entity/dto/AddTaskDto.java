package com.levin.core.entity.dto;

import lombok.Data;

@Data
public class AddTaskDto {
    private int idx;
    private double sum;

    public AddTaskDto(int idx, double sum) {
        this.idx = idx;
        this.sum = sum;
    }
}
