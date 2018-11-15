package com.zebra.zebraclient.dal.dataobject;

import java.math.BigDecimal;

import lombok.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 热点DO
 * 
 * @author owen
 *
 */
@Data
public class SiteDO {

    private Long       id;
    private String     siteName;
    private String     siteAddress;
    private Integer    siteStatus;
    private Long       serviceStationId;
    private String     serviceStationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String     provinceCode;
    private String     provinceName;
    private String     cityCode;
    private String     cityName;
    private String     zoneCode;
    private String     zoneName;
    private Integer    bikeNum;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
