package com.zebra.zebraclient.dal.dataobject;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户订单DO
 * 
 * @author owen
 *
 */
@Data
public class UserOrderDO implements Serializable {

    // `order_id` bigint(20) unsigned NOT NULL COMMENT '订单ID',
    // `uid` bigint(20) NOT NULL COMMENT '用户ID',
    // `nick_name` varchar(20) DEFAULT NULL COMMENT '用户昵称',
    // `order_type` tinyint(3) NOT NULL COMMENT '订单类型 (1、线上预约订单 2、线下订单)',
    // `order_status` tinyint(3) NOT NULL COMMENT '订单状态 (1、预约到期失效 2、预约取消失效 3、正常骑行中 4、结束骑行)',
    // `pay_type` tinyint(3) NOT NULL COMMENT '支付类型 (1、支付宝 2、微信）',
    // `pay_status` tinyint(3) DEFAULT NULL COMMENT '支付状态 (1、未支付 2、已支付)',
    // `start_latitude` decimal(20,17) DEFAULT NULL COMMENT '开始位置经度',
    // `start_longitude` decimal(20,17) DEFAULT NULL COMMENT '开始位置纬度',
    // `strat_address` varchar(11) DEFAULT NULL COMMENT '开始位置地址',
    // `end_latitude` decimal(20,17) DEFAULT NULL COMMENT '结束位置经度',
    // `end_longitude` decimal(20,17) DEFAULT NULL COMMENT '结束位置纬度',
    // `end_address` int(11) DEFAULT NULL COMMENT '结束位置地址',
    // `bike_code` varchar(20) DEFAULT NULL COMMENT '电车编码',
    // `battery_code` varchar(20) DEFAULT NULL COMMENT '电池编码',
    // `total_amount` decimal(10,2) DEFAULT NULL COMMENT '订单总金额',
    // `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '实际支付金额',
    // `pay_order_no` varchar(50) DEFAULT NULL COMMENT '支付单号',
    // `km_unit_price` decimal(10,2) DEFAULT NULL COMMENT '里程单价(元)',
    // `hour_unit_price` decimal(10,2) DEFAULT NULL COMMENT '时间单价(元)',
    // `start_time` int(11) COMMENT '开始时间',
    // `end_time` int(11) COMMENT '结束时间',
    // `book_time` int(11) COMMENT '预约时间',
    // `book_cancel_time` int(11) COMMENT '预约取消时间',
    // `share_click_num` int(11) DEFAULT NULL COMMENT '分享点击次数',

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String            orderId;
    private Long              uid;
    private String            nickName;
    private Integer           orderType;
    private Integer           orderStatus;
    private Integer           payType;
    private Integer           payStatus;
    private BigDecimal        startLatitude;
    private BigDecimal        startLongitude;
    private String            stratAddress;
    private BigDecimal        endLatitude;
    private BigDecimal        endLongitude;
    private String            endAddress;
    private String            bikeCode;
    private String            batteryCode;
    private Double            totalDistance;
    private Double            totalAmount;
    private Double            payAmount;
    private String            payOrderNo;
    private Double            kmUnitPrice;
    private Double            hourUnitPrice;
    private Integer           startTime;
    private Integer           endTime;
    private Integer           bookTime;
    private Integer           bookCancelTime;
    private Integer           shareClickNum;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
