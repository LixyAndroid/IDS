package com.levin.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;


/**
 * 运输任务
 */
@Data
public class TransportTask implements Cloneable {
    @Excel(name = "序号", orderNum = "1", mergeVertical = true, isImportField = "area")
    private String id;
    @Excel(name = "大区", orderNum = "2", mergeVertical = true, isImportField = "area")
    private String area;
    @Excel(name = "发运部", orderNum = "3", mergeVertical = true, isImportField = "start")
    private String start;
    @Excel(name = "品牌", orderNum = "4", mergeVertical = true, isImportField = "brand")
    private String brand;
    @Excel(name = "车型", orderNum = "5", mergeVertical = true, isImportField = "type")
    private String type;
    @Excel(name = "压板天数", orderNum = "6", mergeVertical = true, isImportField = "platenDate")
    private Integer platenDate;
    @Excel(name = "压板省份", orderNum = "7", mergeVertical = true, isImportField = "endPro")
    private String endPro;
    @Excel(name = "压板城市", orderNum = "8", mergeVertical = true, isImportField = "endCity")
    private String endCity;
    @Excel(name = "商品车压板数量", orderNum = "9", mergeVertical = true, isImportField = "platenNum")
    private Double platenNum;
    @Excel(name = "压板原因", orderNum = "10", mergeVertical = true, isImportField = "reason")
    private String reason;
    @Excel(name = "发运部经度", orderNum = "11", mergeVertical = true, isImportField = "lng1")
    private double lng1;
    @Excel(name = "压板城市经度", orderNum = "13", mergeVertical = true, isImportField = "lng2")
    private double lng2;
    @Excel(name = "发运部纬度", orderNum = "12", mergeVertical = true, isImportField = "lat1")
    private double lat1;
    @Excel(name = "压板城市纬度", orderNum = "14", mergeVertical = true, isImportField = "lat2")
    private double lat2;
    @Excel(name = "订单金额", orderNum = "15", mergeVertical = true, isImportField = "amount")
    private double amount;
    @Excel(name = "时间窗", orderNum = "16", mergeVertical = true, isImportField = "tw")
    private int tw;

    @Override
    public TransportTask clone() {
        try {
            Object clone = super.clone();
            return (TransportTask) clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
