package com.zebra.zebraclient.biz.pojo.viewobject;

import java.math.BigDecimal;
import java.util.List;

import com.zebra.zebraclient.dal.dataobject.businessObject.BikeLocusBO;

import lombok.Data;

/**
 * 获取当前行驶数据
 *
 * @author owen
 */
// {
// "latitude":30.3,//起始点经度
// "longitude":120.2,//起始点纬度
// "mobiledTime":"1.6千米+9分钟",//已经骑行 时间+路程
// "price":10.2//当前总费用 单位：元
// "leftMiles":10.2//预计可行驶距离 单位：千米
// "battery":90 //剩余电量
// "isOutFench":1//1：超出服务区 0：未超出
// "routeList":[{point1,point2}]
// }
// 备注：point为{"latitude":double,"longitude":double}
// "orderId":11111111
// //"price":10.2//当前总费用 单位：元
// //"priceDesc":String,//计费规则
// //"mobiledTime":8,//骑行用时 单位：分
// "distancePrice":0 //距离产生的价格，默认0
// "timePrice":12.1 //时间产生的价格
// "ridingDistAnce":9//共骑行距离 单位：KM
@Data
public class RidingDetailVO {


    private BigDecimal        currentLatitude;
    private BigDecimal        currentLongitude;

    private BigDecimal        latitude;
    private BigDecimal        longitude;
    private String            mobiledTime;
    private int               minite;
    private Double            price;
    private Double            leftMiles;
    private Integer           elecQuanlity;
    private String            priceDesc;
    private Integer           isOutFench    = 0;  // 设置默认值
    private List<BikeLocusBO> routeList;

    /** 订单状态 (1、预约成功 2、预约到期失效 3、预约取消失效 4、正常骑行中 5、结束骑行),只会有4，5 */
    private Integer           orderStatus   = 4;
    private String            orderId;
    private Double            distancePrice = 0.0;
    private Double            timePrice;
    private Double            ridingDistAnce;

    /**
     * 车子模型，两轮车、三轮车
     */
    private String            bikeModel;

    private String            bikeNo;

}
