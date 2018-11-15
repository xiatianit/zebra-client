package com.zebra.zebraclient.dal.dataobject.businessObject;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BikeBO {

    private BigDecimal localLongitude;

    private BigDecimal localLatitude;

    private Integer    radius;

    private String     cityCode;

    private Integer    zoneCode;
}
