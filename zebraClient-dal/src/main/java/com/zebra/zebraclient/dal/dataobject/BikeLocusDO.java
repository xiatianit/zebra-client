package com.zebra.zebraclient.dal.dataobject;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 电车电池轨迹DO
 * 
 * @author owen
 *
 */
@Data
public class BikeLocusDO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Long              id;
    private String            bikeCode;
    private String            batteryCode;
    private BigDecimal        latitude;
    private BigDecimal        longitude;
    private Integer           batteryElec;
    private Double            restMileage;
    private Integer           receiveTime;
    private String            orderId;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
