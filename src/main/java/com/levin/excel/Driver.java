package com.levin.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;


/**
 * 司机/车辆信息
 */
@Data
public class Driver {
    @Excel(name = "序号", orderNum = "1", mergeVertical = true, isImportField = "id")
    private String id;
    @Excel(name = "所属大区", orderNum = "2", mergeVertical = true, isImportField = "area")
    private String area;
    @Excel(name = "所属车队", orderNum = "3", mergeVertical = true, isImportField = "fleet")
    private String fleet;
    @Excel(name = "主车车牌", orderNum = "4", mergeVertical = true, isImportField = "license")
    private String license;
    @Excel(name = "司机", orderNum = "5", mergeVertical = true, isImportField = "name")
    private String name;
    @Excel(name = "电话", orderNum = "6", mergeVertical = true, isImportField = "phone")
    private String phone;
    @Excel(name = "在途位置", orderNum = "7", mergeVertical = true, isImportField = "addr")
    private String addr;
    @Excel(name = "滞留天数", orderNum = "8", mergeVertical = true, isImportField = "retention")
    private Integer retention;
    @Excel(name = "滞留原因", orderNum = "9", mergeVertical = true, isImportField = "reason")
    private String reason;
    @Excel(name = "经度", orderNum = "10", mergeVertical = true, isImportField = "lng")
    private double lng;
    @Excel(name = "纬度", orderNum = "11", mergeVertical = true, isImportField = "lat")
    private double lat;
    @Excel(name = "车型", orderNum = "12", mergeVertical = true, isImportField = "type")
    private String type;

}
