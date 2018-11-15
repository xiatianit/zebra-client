package com.zebra.zebraclient.biz.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zebra.zebraclient.dal.dataobject.UserAddressDO;

public interface UserAddressService {

    /****************** 基础的增删改查 ********************/
    /**
     * 插入
     *
     * @param userExtendDO
     * @return
     */
    void addUserAddress(UserAddressDO userAddressDO);

    /**
     * 删除
     * @param uid
     * @param addressType
     */
    void deleteUserAddressByAddTypeAndUid(Long uid, Integer addressType);

    /**
     * 通过类型、uid来处理
     * 
     * @param uid
     * @param addressType
     * @return
     */
    UserAddressDO queryUserAddressByAddTypeAndUid(@Param("uid") Long uid, @Param("addressType") Integer addressType);

    /**
     * 获取用户地址信息
     * @param userAddressDO
     * @return
     */
    List<UserAddressDO> queryUserAddressByCondition(UserAddressDO userAddressDO);
}
