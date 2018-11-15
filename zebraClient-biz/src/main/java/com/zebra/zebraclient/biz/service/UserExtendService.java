package com.zebra.zebraclient.biz.service;

import com.zebra.zebraclient.dal.dataobject.UserExtendDO;

/**
 * 用户累计行程
 * @author xiatian
 *
 */
public interface UserExtendService {

	
    /**
     * 新增用户行程累计
     *
     * @param userExtendDO
     * @return
     */
    void addUserExtend(UserExtendDO userExtendDO);

    /**
     * 修改用户行程累计
     *
     * @param userExtendDO
     * @return
     */
    void editUserExtend(UserExtendDO userExtendDO);
    
    
    /**
     * 获取用户行程累计
     * @param uid
     * @return
     */
    UserExtendDO queryUserExtendByUid(Long uid);
	
	
	
	
}
