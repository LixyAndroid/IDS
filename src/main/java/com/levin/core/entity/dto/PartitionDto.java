package com.levin.core.entity.dto;

import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class PartitionDto {
    /**
     * 车辆列表
     */
    private List<DriverDto> driverList;
    /**
     * 任务列表
     */
    private List<TransportTask> taskList;

    public PartitionDto(List<DriverDto> driverList, List<TransportTask> taskList) {
        this.driverList = driverList;
        this.taskList = taskList;
    }

    public List<Driver> getDrivers() {
        List<Driver> result = new ArrayList<>();
        for (DriverDto dt : driverList) {
            result.add(dt.getDriver());
        }
        return result;
    }

    public List<DriverDto> getDriverList() {
        return driverList;
    }

    public void setDriverList(List<DriverDto> driverList) {
        this.driverList = driverList;
    }

    public List<TransportTask> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<TransportTask> taskList) {
        this.taskList = taskList;
    }
}
