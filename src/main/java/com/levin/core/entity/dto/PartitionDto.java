package com.levin.core.entity.dto;

import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import lombok.Data;

import java.util.List;

@Data
public class PartitionDto {
    /**
     * 车辆列表
     */
    private List<Driver> driverList;
    /**
     * 任务列表
     */
    private List<TransportTask> taskList;

    public PartitionDto(List<Driver> driverList, List<TransportTask> taskList) {
        this.driverList = driverList;
        this.taskList = taskList;
    }

    public PartitionDto() {
    }
}
