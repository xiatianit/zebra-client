package com.zebra.zebraclient.api.controller;

import com.zebra.zebraclient.biz.service.UserService;
import com.zebra.zebraclient.dal.dataobject.UserBaseDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.zebra.pony.common.model.Result;
import com.zebra.pony.common.utils.ResultUtils;
import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.api.helper.TokenHelper;
import com.zebra.zebraclient.biz.enums.ResultErrorEnum;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.service.LoginService;
import com.zebra.zebraclient.dal.dataobject.DeviceDO;

/**
 * 用户登录／token 控制层
 * 
 * @author owen
 *
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginService        loginService;

    @Autowired
    private TokenHelper         tokenHelper;

    @Autowired
    private UserService userService;

    /**
     * 获取登录验证码 用户发送手机号
     * 
     * @param telphone
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getLgnAuthCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> getLgnAuthCode(@RequestParam("phone") String telphone) {
        logger.info("发送短信,telphone={}", telphone);
        try {
            loginService.doGetLgnAuthCode(telphone);
            return ResultUtils.genResultWithSuccess(true);
        } catch (Exception e) {
            logger.error("发送短信失败,telphone={},e={}", telphone, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 登录
     * 
     * @param deviceId 设备ID
     * @param appVersion 应用版本好
     * @param deviceType 设备系统类型 1-安卓 2-IOS
     * @param telphone 手机号码
     * @param loginAuthCode 登录验证码
     * @param receiveId 接受ID
     * @return
     */
    @RequestMapping(value = "/loginIn", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doLoginIn(@RequestParam("device_id") String deviceId, @RequestParam("app_version") String appVersion,
            @RequestParam("device_type") Integer deviceType, @RequestParam("phone") String telphone,
            @RequestParam("loginAuthCode") String loginAuthCode, @RequestParam("receiveId") String receiveId) {
        DeviceDO deviceDO = new DeviceDO();
        try {
            deviceDO.setDeviceType(deviceType);
            deviceDO.setAppVersion(appVersion);
            deviceDO.setDeviceId(deviceId);
            deviceDO.setUid(0L);
            deviceDO.setCurrentReceiveId(receiveId);
            logger.info("创建新token信息,deviceDO={}", JsonUtil.getJsonFromObject(deviceDO));
            return loginService.doLoginIn(deviceDO, telphone, loginAuthCode);
        } catch (Exception e) {
            logger.error("创建新token信息失败,deviceDO={},e={}", JsonUtil.getJsonFromObject(deviceDO), e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 刷新token 每次登录打开app时进行操作
     * 
     * @param deviceId
     * @param appVersion
     * @param deviceType
     * @param token
     * @return
     */
    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doRefreshToken(@RequestParam("device_id") String deviceId, @RequestParam("app_version") String appVersion,
            @RequestParam("device_type") Integer deviceType, @RequestParam("token") String token) {
        DeviceDO deviceDO = new DeviceDO();
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            deviceDO.setDeviceType(deviceType);
            deviceDO.setAppVersion(appVersion);
            deviceDO.setDeviceId(deviceId);
            deviceDO.setToken(token);
            logger.info("刷新token信息,deviceDO={}", JsonUtil.getJsonFromObject(deviceDO));
            return loginService.doRefreshToken(deviceDO);
        } catch (Exception e) {
            logger.error("刷新token信息失败,deviceDO={},e={}", JsonUtil.getJsonFromObject(deviceDO), e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 用户退出
     * @param token  token
     * @return
     */
    @RequestMapping(value = "/loginOut", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doLoginOut(@RequestParam("token") String token) {
        logger.info("退出登录token={}", token);
        DeviceDO deviceDO = new DeviceDO();
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            loginService.doLoginOut(token);
            return ResultUtils.genResultWithSuccess();
        } catch (Exception e) {
            logger.error("创建新token信息失败,deviceDO={},e={}", JsonUtil.getJsonFromObject(deviceDO), e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

}
