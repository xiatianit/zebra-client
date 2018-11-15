package com.zebra.zebraclient.biz.service;

import com.zebra.pony.common.model.Result;
import com.zebra.zebraclient.dal.dataobject.DeviceDO;

/**
 * 用户token服务
 * @author owen
 *
 */
public interface LoginService {
    
    /**
     * 发送短信验证码
     * @param telphone
     * @return
     */
    public void doGetLgnAuthCode(String telphone);
    
    /**
     * 登录信息
     * @param deviceDO
     */
    public Result<?> doLoginIn(DeviceDO deviceDO,String telphone,String loginAuthCode);
    
    /**
     * 退出登录
     * @param tokenStr
     */
    public void doLoginOut(String tokenStr);
    
    /**
     * 刷新token
     * @param deviceDO
     * @return
     */
    public Result<?> doRefreshToken(DeviceDO deviceDO);
}
