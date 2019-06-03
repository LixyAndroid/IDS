package com.levin.core.entity.dto;

import com.levin.excel.Driver;
import lombok.Data;

@Data
public class DriverDto {
    private Driver driver;
    private double distance;

    public DriverDto(Driver driver, double distance) {
        this.driver = driver;
        this.distance = distance;
    }
}
