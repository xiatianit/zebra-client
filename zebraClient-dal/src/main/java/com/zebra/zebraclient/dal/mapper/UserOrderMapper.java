package com.zebra.zebraclient.dal.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zebra.zebraclient.dal.dataobject.UserOrderDO;

public interface UserOrderMapper {

    /****************** 基础的增删改查 ********************/
    /**
     * 查询
     *
     * @param id
     * @return
     */
    UserOrderDO selectByPk(@Param("orderId") String orderId);

    /**
     * 插入
     *
     * @param userOrderDO
     * @return
     */
    int insert(UserOrderDO userOrderDO);

    /**
     * 更新
     *
     * @param userOrderDO
     * @return
     */
    int update(UserOrderDO userOrderDO);

    /**
     * 通过uid来获取用户订单信息
     * 
     * @param uid
     * @return
     */
    List<UserOrderDO> selectByUid(@Param("uid") Long uid);

    /**
     * 通过uid来获取用户未支付或者骑行中的订单
     * 
     * @param uid
     * @return
     */
    List<UserOrderDO> selectUncomplatedByUid(@Param("uid") Long uid);

    /**
     * 用户uid ,bikeCode，该车是否在行驶当中。
     * 
     * @param uid
     * @param bikeCode
     * @return
     */
    List<UserOrderDO> selectBikeCurrentRidingStatus(@Param("uid") Long uid, @Param("bikeCode") String bikeCode);

}
