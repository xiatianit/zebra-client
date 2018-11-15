package com.zebra.zebraclient.biz.pojo.viewobject;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 未完成的订单
 *
 * @author owen
 */
@Data
@AllArgsConstructor
public class OrderUnComplatedVO {

    /**
     * 0表示没有,1表示还有骑行中进行中的订单,2表示还有未支付订单
     */
    private int status = 0;

    private String orderId;

    private String bikeCode;

    private String bikeNo;

    public OrderUnComplatedVO(int status, String orderId, String bikeCode) {
        this.status = status;
        this.orderId = orderId;
        this.bikeCode = bikeCode;
    }

    public OrderUnComplatedVO(int status, String orderId, String bikeCode, String bikeNo) {
        this.status = status;
        this.orderId = orderId;
        this.bikeCode = bikeCode;
        this.bikeNo = bikeNo;
    }

    // 如果是2，是有未完成的订单，则把下面内容一并返回
    // private String orderId;
    private Double price;
    private String priceDesc;
    private Double distancePrice = 0.0;
    private Double timePrice;
    private Integer mobiledTime;
    private Double ridingDistAnce;

    // "price":10.2//当前总费用 单位：元
    // "priceDesc":String,//计费规则
    // "distancePrice":0
    // "timePrice":12.1
    // "mobiledTime":8,//骑行用时 单位：分
    // "ridingDistAnce":9//共骑行距离 单位：KM
    //

}
