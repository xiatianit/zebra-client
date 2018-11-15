package com.zebra.zebraclient.dal.mapper;

import org.apache.ibatis.annotations.Param;

import com.zebra.zebraclient.dal.dataobject.UserBaseDO;

public interface UserBaseMapper {
    
    /******************基础的增删改查********************/
    /**
     * 查询
     *
     * @param id
     * @return
     */
    UserBaseDO selectByPk(@Param("uid") Long uid);
    
    /**
     * 查询
     *
     * @param id
     * @return
     */
    UserBaseDO selectByPhone(@Param("phone") String phone);

    /**
     * 插入
     *
     * @param userBaseDO
     * @return
     */
    int insert(UserBaseDO userBaseDO);

    /**
     * 更新
     *
     * @param userBaseDO
     * @return
     */
    int update(UserBaseDO userBaseDO);
    
    
    /**
     * 查询,关联信息也查出来
     *
     * @param id
     * @return
     */
    UserBaseDO selectRelByPk(@Param("uid") Long uid);

}
