package com.zebra.zebraclient.dal.mapper;

import org.apache.ibatis.annotations.Param;

import com.zebra.zebraclient.dal.dataobject.UserExtendDO;

public interface UserExtendMapper {

    /****************** 基础的增删改查 ********************/
    /**
     * 插入
     *
     * @param userExtendDO
     * @return
     */
    int insert(UserExtendDO userExtendDO);

    /**
     * 更新
     *
     * @param userExtendDO
     * @return
     */
    int update(UserExtendDO userExtendDO);
    
    
    /**
     * 通过UID去查询用户扩展信息
     * @param uid
     * @return
     */
    UserExtendDO selectByUid(@Param(value="uid")Long uid);
}
