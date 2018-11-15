package com.zebra.zebraclient.biz.pojo.viewobject;

import lombok.Data;

/**
 * 获取附近车辆信息
 *
 * @author owen
 */
@Data
public class BikeRelInfoVO {

    /*** "status" :0, //0表示正常,1表示不能使用 */

    private Integer status;

    private String bikeCode;

    private Integer leftElec;

    private Double leftMiles;

    private String priceDesc;

    private String bikeNo;
}
