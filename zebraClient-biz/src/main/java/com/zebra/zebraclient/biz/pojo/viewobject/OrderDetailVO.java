package com.zebra.zebraclient.biz.pojo.viewobject;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.zebra.zebraclient.dal.dataobject.businessObject.BikeLocusBO;

/**
 * 订单详情
 * 
 * @author owen
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderDetailVO extends OrderVO {
    // "totalTime":"11分02秒"//总用时
    // "speed":"24.6 千米/小时"//速度
    private String            totalTime;
    private String            speed;

    private String            portrait;
    private String            shareImg;
    private Integer           startBattery;
    private Integer           endBattery;
    private List<BikeLocusBO> routeList;

    public OrderDetailVO(String orderId, String startAddress, String endAddress, String orderTime, Double totalDistance, Double totalPrice) {
        this.orderId = orderId;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.orderTime = orderTime;
        this.totalDistance = totalDistance;
        this.totalPrice = totalPrice;
    }

}
