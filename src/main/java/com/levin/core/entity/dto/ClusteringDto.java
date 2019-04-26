package com.levin.core.entity.dto;

import com.levin.excel.TransportTask;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚类结果
 */
@Data
public class ClusteringDto {
    private List<TransportTask> mid = new ArrayList<>();
    private List<TransportTask> in = new ArrayList<>();
    private List<TransportTask> out = new ArrayList<>();
    private List<TransportTask> all = new ArrayList<>();
}
