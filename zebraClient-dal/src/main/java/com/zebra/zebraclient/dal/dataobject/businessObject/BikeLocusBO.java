package com.zebra.zebraclient.dal.dataobject.businessObject;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 电车电池轨迹DO
 * @author owen
 *
 */
@Data
public class BikeLocusBO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private BigDecimal        latitude;
    private BigDecimal        longitude;

}
