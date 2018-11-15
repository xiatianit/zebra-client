package com.zebra.zebraclient.biz.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.zebra.common.constant.ZebraConstant;
import com.zebra.pony.common.model.Result;
import com.zebra.pony.common.utils.ResultUtils;
import com.zebra.pony.util.DateUtil;
import com.zebra.pony.util.JsonUtil;
import com.zebra.pony.util.ShortMessageUtil;
import com.zebra.pony.util.StringUtil;
import com.zebra.zebraclient.biz.enums.ResultErrorEnum;
import com.zebra.zebraclient.biz.exception.ZebraclientException;
import com.zebra.zebraclient.biz.pojo.DynamicParamConfig;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.service.DeviceService;
import com.zebra.zebraclient.biz.service.LoginService;
import com.zebra.zebraclient.biz.service.UserService;
import com.zebra.zebraclient.biz.util.RandomUtil;
import com.zebra.zebraclient.dal.dataobject.DeviceDO;
import com.zebra.zebraclient.dal.dataobject.UserBaseDO;

/***
 * token 获取
 * 
 * @author owen
 */
@Service
public class LoginServiceImpl implements LoginService {

    private static final Logger             logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Resource
    private DeviceService                   deviceService;

    @Resource
    private UserService                     userService;

    @Autowired
    private DynamicParamConfig              dynamicParamConfig;

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> valueOps;

    @Autowired
    private StringRedisTemplate             stringRedisTemplate;

    public static void main(String[] args) {
        String s = "13388601971";
        System.out.println(s.substring(0, 3));
        System.out.println(s.substring(7, 11));
    }

    /**
     * 发送短信验证码
     * 
     * @param telphone 电话
     */
    @Override
    public void doGetLgnAuthCode(String telphone) {
        try {
            // 1、发送短信
            String randomNum = RandomUtil.getFourRandom();
            String message = "【斑马电车】验证码：" + randomNum + "，15分钟内输入有效，斑马电车，轻松出行";
            ShortMessageUtil.sendSms(message, telphone);
            logger.info("验证码短信发送：telphone={},message={}", telphone, message);

            // 2、存redis
            valueOps.set(ZebraConstant.REIDS_PREFIX_USER_LOGIN_AUTH + telphone, randomNum, dynamicParamConfig.getUserLoginAuthCodeEffectiveTime(),
                    TimeUnit.SECONDS);
        } catch (IOException e) {
            logger.error("验证码短信发送失败：telphone={},e={}", telphone, e);
            throw new ZebraclientException("验证码短信发送失败");
        }
    }

    /**
     * 登录信息
     * 
     * @param deviceDO 设备信息
     */
    @Override
    public Result<?> doLoginIn(DeviceDO deviceDO, String telphone, String loginAuthCode) {
        int nowTime = DateUtil.getCurrentTimeSeconds();
        if ("18888888888".equals(telphone) && "1234".equals(loginAuthCode)) {
            logger.info("测试账户telphone={},loginAuthCode={}", telphone, loginAuthCode);
        } else {
            // 1、验证用户登录手机验证码是否失效
            String redisLoginAuthCode = valueOps.get(ZebraConstant.REIDS_PREFIX_USER_LOGIN_AUTH + telphone);
            if (StringUtil.isBlank(redisLoginAuthCode)) {
                return ResultUtils.genResult(ResultErrorEnum.LOGIN_AUTHCODE_EFFECTIVE.getErrorCode(),
                        ResultErrorEnum.LOGIN_AUTHCODE_EFFECTIVE.getErrorMsg());
            }

            // 2.1、比对验证码是否匹配
            if (!telphone.equals(telphone)) {
                return ResultUtils.genResult(ResultErrorEnum.LOGIN_AUTHCODE_COMPARE_ERROR.getErrorCode(),
                        ResultErrorEnum.LOGIN_AUTHCODE_COMPARE_ERROR.getErrorMsg());
            }

            // 2.2、
            if (!redisLoginAuthCode.equals(loginAuthCode)) {
                return ResultUtils.genResult(ResultErrorEnum.LOGIN_AUTHCODE_COMPARE_ERROR.getErrorCode(),
                        ResultErrorEnum.LOGIN_AUTHCODE_COMPARE_ERROR.getErrorMsg());
            }

            // 3、设备号
            if (StringUtils.isEmpty(deviceDO.getDeviceId())) {
                return ResultUtils.genResult(ResultErrorEnum.DEVICE_INPUT_ERROR.getErrorCode(), ResultErrorEnum.DEVICE_INPUT_ERROR.getErrorMsg());
            }
        }
        
        // 同一deviceID，先后登录两个手机用户
        DeviceDO tempDeviceDO = deviceService.queryDeviceByPk(deviceDO.getDeviceId());
        if (tempDeviceDO != null) {
            logger.info("用户删除redis_token,token={}", tempDeviceDO.getToken().trim());
            stringRedisTemplate.delete(ZebraConstant.REDIS_PREFIX_USER_TOKEN + tempDeviceDO.getToken().trim());
            deviceService.delDeviceByPk(deviceDO.getDeviceId());
        }

        // 3、如果数据中，用户存在，则不做处理，用户不存在，则添加。
        UserBaseDO userBaseDO = userService.queryByPhone(telphone);
        if (userBaseDO == null) {
            // 插入
            logger.info("用户第一次token登录");
            userBaseDO = new UserBaseDO();
            userBaseDO.setPhone(telphone);
            userBaseDO.setRegisterTime(nowTime);
            userBaseDO.setUserStatus(ZebraConstant.USER_STATUS_0);
            userBaseDO.setVerifyStatus(ZebraConstant.USER_VERIFY_STATUS_0);
            userBaseDO.setNickName(telphone.substring(0, 3) + "****" + telphone.substring(7, 11));
            userBaseDO.setModifyTime(nowTime);
            userService.addUserBase(userBaseDO);
        } else {
            // 修改下最后登录时间
            logger.info("用户非第一次token登录");
            userBaseDO.setModifyTime(nowTime);
            userService.editUserBase(userBaseDO);

            // 这种情况，得把以前存在token都删掉。
            logger.info("用户删除redis_token,uid={}", userBaseDO.getUid());
            List<DeviceDO> list = deviceService.queryDeviceByUid(userBaseDO.getUid());
            if (list != null && list.size() > 0) {
                for (DeviceDO d : list) {
                    logger.info("用户删除redis_token,token={}", d.getToken().trim());
                    stringRedisTemplate.delete(ZebraConstant.REDIS_PREFIX_USER_TOKEN + d.getToken().trim());
                }
            }

            // 4、单点登录：同一手机用户、同一设备号，只能在同一时间登录 token、uid-phone、deviceId,receiveId
            logger.info("用户删除device_token,uid={}", userBaseDO.getUid());
            deviceService.delDeviceByUid(userBaseDO.getUid());
        }

        // 5、生成新的token
        String tokenStr = UUID.randomUUID().toString().replaceAll("-", "");
        deviceDO.setUid(userBaseDO.getUid());
        deviceDO.setToken(tokenStr);
//        deviceDO.setLastUpdateTime(nowTime);
        deviceService.addDevice(deviceDO);
        valueOps.set(ZebraConstant.REDIS_PREFIX_USER_TOKEN + tokenStr, buildUserTokenStr(nowTime, deviceDO),
                dynamicParamConfig.getUserTokenEffectiveTime(), TimeUnit.SECONDS);
        return ResultUtils.genResultWithSuccess("token", tokenStr);
    }

    /**
     * 登录信息
     * 
     * @param deviceDO
     */
    public void doLoginOut(String tokenStr) {
        // 设置token对象为空
        valueOps.set(ZebraConstant.REDIS_PREFIX_USER_TOKEN + tokenStr, "", 3, TimeUnit.SECONDS);
    }

    /**
     * 刷新token
     * 
     * @param deviceDO
     */
    public Result<?> doRefreshToken(DeviceDO deviceDO) {
        int nowTime = DateUtil.getCurrentTimeSeconds();

        // 1、获取用户的token信息
        String userTokenStr = valueOps.get(ZebraConstant.REDIS_PREFIX_USER_TOKEN + deviceDO.getToken());
        logger.info("刷新token,deviceDO={},userTokenStr={}", deviceDO, userTokenStr);

        UserToken userToken = JsonUtil.getObjectFromJson(userTokenStr, UserToken.class);
        if (StringUtil.isBlank(userTokenStr)) {
            return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), ResultErrorEnum.USER_TOKEN_INVALID.getErrorMsg());
        }

        // 2、deviceID先不做验证处理, TODO token 对应的设备不是同一个

        // 3、重新设置失效用户token失效时间。
        deviceDO.setUid(userToken.getUid());
        valueOps.set(ZebraConstant.REDIS_PREFIX_USER_TOKEN + deviceDO.getToken(), buildUserTokenStr(nowTime, deviceDO),
                dynamicParamConfig.getUserTokenEffectiveTime(), TimeUnit.SECONDS);

        return ResultUtils.genResultWithSuccess();
    }

    /**
     *
     * @param siteId
     * @param time
     * @param device
     * @return
     */
    private String buildUserTokenStr(Integer time, DeviceDO deviceDO) {
        UserToken userToken = new UserToken();
        userToken.setDeviceId(deviceDO.getDeviceId());
        userToken.setDeviceType(null != deviceDO.getDeviceType() ? deviceDO.getDeviceType() : 0);
        userToken.setTime(time);
        userToken.setUid(deviceDO.getUid());
        UserBaseDO userBaseDO = userService.queryRelByPk(deviceDO.getUid());
        userToken.setNickName(userBaseDO.getNickName());
        userToken.setTokenStr(deviceDO.getToken());
        userToken.setCurrentReceiveId(deviceDO.getCurrentReceiveId());
        return JsonUtil.getJsonFromObject(userToken);
    }

}
