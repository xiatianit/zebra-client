package com.zebra.zebraclient.biz.pojo.viewobject;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 订单详情
 * 
 * @author owen
 *
 */
@Data
@AllArgsConstructor
public class OrderVO {

    protected String orderId;

    protected String startAddress;

    protected String endAddress;

    protected String orderTime;

    protected Double totalDistance;

    protected Double totalPrice;

    public OrderVO() {
    }

}
