package com.zebra.zebraclient.biz.service;

import com.zebra.zebraclient.dal.dataobject.UserBaseDO;


/**
 * 用户
 * @author owen
 */
public interface UserService {
    
    /******************基础的增删改查********************/
    /**
     * 查询
     *
     * @param
     * @return
     */
    UserBaseDO queryByPk(Long uid);
    
    /**
     * 查询
     *
     * @param
     * @return
     */
    UserBaseDO queryByPhone(String phone);
    
    /**
     * 查询,关联信息也查出来
     *
     * @param
     * @return
     */
    UserBaseDO queryRelByPk(Long uid);

    /**
     * 插入
     *
     * @param userBaseDO
     * @return
     */
    void addUserBase(UserBaseDO userBaseDO);

    /**
     * 更新
     *
     * @param userBaseDO
     * @return
     */
    void editUserBase(UserBaseDO userBaseDO);

    /**
     * 用户进行实名认证
     * @param realName
     * @param idcard
     */
    String doRealName(Long uid,String realName,String idcard);

}
