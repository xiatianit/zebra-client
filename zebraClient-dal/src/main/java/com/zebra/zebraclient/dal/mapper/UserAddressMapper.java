package com.zebra.zebraclient.dal.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zebra.zebraclient.dal.dataobject.UserAddressDO;

public interface UserAddressMapper {

    /****************** 基础的增删改查 ********************/
    /**
     * 插入
     *
     * @param userExtendDO
     * @return
     */
    int insert(UserAddressDO userAddressDO);

    /**
     * 更新
     *
     * @param userExtendDO
     * @return
     */
    int update(UserAddressDO userAddressDO);
    
    /**
     * 删除
     * @param uid
     * @param addressType
     * @return
     */
    int deleteByAddTypeAndUid(@Param("uid") Long uid, @Param("addressType") Integer addressType);

    /**
     * 通过类型、uid来处理
     * 
     * @param uid
     * @param addressType
     * @return
     */
    UserAddressDO selectByAddTypeAndUid(@Param("uid") Long uid, @Param("addressType") Integer addressType);

    List<UserAddressDO> selectByCondition(UserAddressDO userAddressDO);
}
