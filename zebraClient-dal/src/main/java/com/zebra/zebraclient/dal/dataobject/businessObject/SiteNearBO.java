package com.zebra.zebraclient.dal.dataobject.businessObject;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SiteNearBO {

    private BigDecimal localLongitude;

    private BigDecimal localLatitude;

    private Integer    radius;

}
