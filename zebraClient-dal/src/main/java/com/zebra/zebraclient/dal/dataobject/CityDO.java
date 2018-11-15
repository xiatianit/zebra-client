package com.zebra.zebraclient.dal.dataobject;

import java.io.Serializable;

import lombok.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 城市DO
 * 
 * @author owen
 *
 */
@Data
public class CityDO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String            cityCode;
    private String            cityName;
    private String            cityAbbr;
    private String            zhSpell;
    private String            provinceCode;
    private String            provinceName;
    private Integer           isHot;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
