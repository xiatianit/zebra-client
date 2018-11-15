package com.zebra.zebraclient.dal.dataobject;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户信用额度DO
 * 
 * @author owen
 *
 */
@Data
public class WalletDO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Long              uid;
    private BigDecimal        balanceAmount;
    private BigDecimal        depositAmount;
    private Integer           depositSource;
    private String            depositAccount;
    private String            depositOrderId;
    
    /** 1:支付宝 2:微信**/
    private Integer           depositPayType; 

    /*** 1:未交押金 2:已交押金 3:退还押金中 */
    private Integer           depositStatus;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
