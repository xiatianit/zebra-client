package com.zebra.zebraclient.biz.pojo.viewobject;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 获取附近车辆信息
 *
 * @author owen
 */
@Data
public class QueryNearBikeRespVO {

    private BigDecimal localLongitude;

    private BigDecimal localLatitude;

    private BigDecimal targetLongitude;

    private BigDecimal targetLatitude;

    private String targetAddressName;

    private String bikeCode;

    private String bikeNo;

    private String batteryCode;

    private Integer leftElec;

    private Double leftMiles;

    private String location;

    private String priceDesc;

    private String expectCost;

    private String minMiles;

    // "bikeNum":123213,//车辆编号
    // "batteryID":100000,//电池id
    // "leftElec":80,//剩余电量 单位：百分比
    // "leftMiles":8,//预计可用里程 单位：千米 小数点1位
    // "location":String,//车辆详细地址
    // "priceDesc":String,//价格描述
    // "expectCost": 10.2 //预计总费用,单位:元 小数点1位
    // "minMiles": 10 //最短距离,单位:千米 小数点1位
}
