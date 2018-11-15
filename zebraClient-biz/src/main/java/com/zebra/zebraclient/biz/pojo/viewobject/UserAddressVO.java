package com.zebra.zebraclient.biz.pojo.viewobject;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户地址信息
 * 
 * @author owen
 *
 */
@Data
@AllArgsConstructor
public class UserAddressVO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8857412801351388964L;

    private Long              id;

    private Long              uid;

    /** 0未设置 1已设置 */
    private Integer           setting;

    /** 地址类型，1家地址 2公司地址 */
    private Integer           addressType;

    private BigDecimal        latitude;

    private BigDecimal        longitude;

    private String            address;

    private String            subAddress;

    /** 高德的字段 */
    private String            poiId;

    public UserAddressVO() {
    }

    public UserAddressVO(Integer setting) {
        this.setting = setting;
    }

}
