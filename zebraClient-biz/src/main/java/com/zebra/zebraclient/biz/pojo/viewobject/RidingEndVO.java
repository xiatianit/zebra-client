package com.zebra.zebraclient.biz.pojo.viewobject;

import lombok.Data;

/**
 * 获取当前行驶数据
 * 
 * @author owen
 */
@Data
public class RidingEndVO {

    private int     isLock        = 1;  // 0:不可以上锁，1:可以上锁
    private String  orderId;
    private Double  price;
    private String  priceDesc;
    private Double  distancePrice = 0.0;
    private Double  timePrice;
    private Integer mobiledTime;
    private Double  ridingDistAnce;
    private String  bikeNo;

    // "orderId":11111111
    // "price":10.2//当前总费用 单位：元
    // "priceDesc":String,//计费规则
    // "mobiledTime":8,//骑行用时 单位：分
    // "distancePrice":0
    // "timePrice":12.1
    // "ridingDistAnce":9//共骑行距离 单位：KM

}
