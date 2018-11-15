package com.zebra.zebraclient.biz.service;

import java.util.List;

import com.zebra.pony.common.model.Result;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderUnComplatedVO;
import com.zebra.zebraclient.biz.pojo.viewobject.RidingDetailVO;
import com.zebra.zebraclient.biz.pojo.viewobject.RidingEndVO;
import com.zebra.zebraclient.dal.dataobject.BikeDO;
import com.zebra.zebraclient.dal.dataobject.businessObject.BikeBO;

/**
 * 电车服务
 * 
 * @author owen
 */
public interface BikeService {

    /**
     * 获取最近的电车信息
     * 
     * @param bikeBO
     * @return
     */
    List<BikeDO> queryNearBike(BikeBO bikeBO);

    /**
     * 根据bike获取车辆信息
     * 
     * @param bikeCode
     * @return
     */
    BikeDO queryBikeRelInfo(String bikeCode);

    /**
     * 根据bike获取车辆信息
     *
     * @param bikeNo
     * @return
     */
    BikeDO queryBikeRelInfoByBikeNo(String bikeNo);

    /***
     * 通知终端发出铃声找出车辆
     * 
     * @param bikeCode
     */
    void doRingSearchBike(String bikeCode);

    /***
     * 开锁
     * 
     * @param bikeCode
     */
    Result<?> doUnLockBike(String bikeCode, UserToken userToken);

    /***
     * 结束骑行，并上锁
     * 
     * @param bikeCode
     */
    Result<RidingEndVO> doEndDriveAndLockBike(String orderId, String bikeCode,Double totalDistance, UserToken userToken);
    
    /**
     * 获取当前行驶数据
     * @param orderId
     * @param bikeCode
     * @param userToken
     */
    Result<RidingDetailVO> getCurrentRideDetail(String orderId, String bikeCode, UserToken userToken);

    /**
     * 临时停车
     * @param bikeCode
     * @param userToken
     * @param orderId
     * @return
     */
    Result<OrderUnComplatedVO> pauseRide(String bikeCode, UserToken userToken, String orderId);

    /**
     * 继续骑行
     * @param bikeNo
     * @param userToken
     * @param orderId
     * @return
     */
    Result<?> continueRide(String bikeNo, UserToken userToken,String orderId);

}
