package com.zebra.zebraclient.biz.service;

import java.util.List;

import com.zebra.zebraclient.biz.pojo.viewobject.OrderDetailVO;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderUnComplatedVO;
import com.zebra.zebraclient.dal.dataobject.UserOrderDO;

/**
 * 用户订单
 * 
 * @author owen
 *
 */
public interface UserOrderService {

    /**
     * 查询
     *
     * @param id
     * @return
     */
    UserOrderDO queryUserOrderByPk(String orderId);

    /**
     * 更新
     *
     * @param userOrderDO
     * @return
     */
    void editUserOrder(UserOrderDO userOrderDO);

    /**
     * 查询用户orederID
     * 
     * @param uid
     * @return
     */
    List<UserOrderDO> queryUserOrderByUid(Long uid);
    
    /**
     * 获取订单信息详情
     * @param orderId
     * @return
     */
    OrderDetailVO queryUserOrderDetailByOrderId(String orderId);
    
    /**
     * 查询用户orederID
     * 用户未完成订单的情况
     * 
     * @param uid
     * @return
     */
    OrderUnComplatedVO doJudgeUsersOrderSituation(Long uid);
    
    /**
     * 用户uid ,bikeCode，该车是否在行驶当中。
     * 行驶当中为true 
     * 飞行驶当中为false
     * 
     * @param uid
     * @param bikeCode
     * @return
     */
    UserOrderDO jugeBikeCurrentRidingStatus(Long uid, String bikeCode);


}
