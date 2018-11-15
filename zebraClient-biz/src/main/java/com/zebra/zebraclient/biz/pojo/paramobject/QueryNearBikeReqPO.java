package com.zebra.zebraclient.biz.pojo.paramobject;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 获取附近车辆信息请求
 *
 * @author owen
 */
@Data
public class QueryNearBikeReqPO {

    private BigDecimal localLongitude;

    private BigDecimal localLatitude;

    private Integer radius;

    private String cityCode;

    private Integer zoneCode;

    private BigDecimal targetLongitude;

    private BigDecimal targetLatitude;

    private String targetAddressName;

    private Integer targetCityCode;

    private Integer targetZoneCode;

    private Double targetDistance;

    public QueryNearBikeReqPO() {
    }

    public QueryNearBikeReqPO(BigDecimal localLongitude, BigDecimal localLatitude, Integer radius, String cityCode, Integer zoneCode) {
        this.localLongitude = localLongitude;
        this.localLatitude = localLatitude;
        this.radius = radius;
        this.cityCode = cityCode;
        this.zoneCode = zoneCode;
    }
}
