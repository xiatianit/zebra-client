package com.zebra.zebraclient.api.controller;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.zebra.common.constant.ZebraConstant;
import com.zebra.pony.common.model.Result;
import com.zebra.pony.common.utils.ResultUtils;
import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.api.helper.TokenHelper;
import com.zebra.zebraclient.biz.enums.ResultErrorEnum;
import com.zebra.zebraclient.biz.pojo.DynamicParamConfig;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.pojo.converter.UserConverter;
import com.zebra.zebraclient.biz.pojo.viewobject.UserDetailVO;
import com.zebra.zebraclient.biz.service.UserService;
import com.zebra.zebraclient.biz.service.WalletService;
import com.zebra.zebraclient.dal.dataobject.UserBaseDO;

/**
 * 用户基本信息controller
 *
 * @author owen
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private DynamicParamConfig dynamicParamConfig;

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> valueOps;

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public Result<UserDetailVO> doGetUserInfo(@RequestParam("token") String token) {
        logger.info("获取用户明细信息：token={}", token);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("UserController.doGetUserInfo:token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            UserBaseDO userBaseDO = userService.queryRelByPk(userToken.getUid());
            UserDetailVO userDetailVO = UserConverter.userDO2UserVO(userBaseDO);

            // 添加用户押金状态
            int walletDepositStatus = walletService.doGetWalletDepositStatus(userToken.getUid());
            userDetailVO.setDepositState(walletDepositStatus);
            return ResultUtils.genResultWithSuccess(userDetailVO);
        } catch (Exception e) {
            logger.error("获取用户明细信息失败,token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 修改用户性别
     *
     * @param token
     * @param sex
     * @return
     */
    @RequestMapping(value = "/sex", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doEditUserSex(@RequestParam("token") String token, @RequestParam("sex") Integer sex) {
        logger.info("修改用户性别：token={}，sex={}", token, sex);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("UserController.doGetUserInfo:token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            UserBaseDO userBaseDO = userService.queryRelByPk(userToken.getUid());
            userBaseDO.setSex(sex);
            userService.editUserBase(userBaseDO);
            return ResultUtils.genResultWithSuccess();
        } catch (Exception e) {
            logger.error("修改用户性别信息失败,token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 修改用户头像
     *
     * @param token
     * @param headPortrait
     * @return
     */
    @RequestMapping(value = "/headPortrait", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doEditHeadPortrait(@RequestParam("token") String token, @RequestParam("headPortrait") String headPortrait) {
        logger.info("修改用户头像：token={}，headPortrait={}", token, headPortrait);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("UserController.doEditHeadPortrait={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            UserBaseDO userBaseDO = userService.queryRelByPk(userToken.getUid());
            userBaseDO.setHeadPortrait(headPortrait);
            userService.editUserBase(userBaseDO);
            return ResultUtils.genResultWithSuccess();
        } catch (Exception e) {
            logger.error("修改用户头像失败,token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 修改用户昵称
     *
     * @param token
     * @param
     * @return
     */
    @RequestMapping(value = "/nickName", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doEditNickName(@RequestParam("token") String token, @RequestParam("nickName") String nickName) {
        logger.info("修改用户昵称：token={}，nickName={}", token, nickName);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("UserController.doEditHeadPortrait={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            UserBaseDO userBaseDO = userService.queryRelByPk(userToken.getUid());
            userBaseDO.setNickName(nickName);

            // 重新设置用户的昵称至token对象中
            userToken.setNickName(nickName);
            valueOps.set(ZebraConstant.REDIS_PREFIX_USER_TOKEN + token, JsonUtil.getJsonFromObject(userToken),
                    dynamicParamConfig.getUserTokenEffectiveTime(), TimeUnit.SECONDS);

            userService.editUserBase(userBaseDO);
            return ResultUtils.genResultWithSuccess();
        } catch (Exception e) {
            logger.error("修改用户昵称失败,token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 获取用户是否实名认证状态
     *
     * @param token
     * @return
     */
    @RequestMapping(value = "/get/realname/status", method = RequestMethod.POST)
    public Result<?> getRealNameStatus(@RequestParam(value = "token", required = true) String token) {
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            UserBaseDO userBaseDO = userService.queryByPk(userToken.getUid());
            logger.info("MapController.getUserRealNameStatus:token={},userToken={},userBaseDO={}", token, JsonUtil.getJsonFromObject(userToken), JsonUtil.getJsonFromObject(userBaseDO));
            return ResultUtils.genResultWithSuccess("realNameStatus",userBaseDO.getVerifyStatus());
        } catch (Exception e) {
            logger.error("查询用户实名认证失败：token={},e={}", token, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 进行实名认证
     *
     * @param token
     * @return
     */
    @RequestMapping(value = "/do/real/name", method = RequestMethod.POST)
    public Result<?> doRealname(@RequestParam(value = "token", required = true) String token,
                                @RequestParam("realName") String realName,
                                @RequestParam("idcard") String idcard) {
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            String result = userService.doRealName(userToken.getUid(), realName, idcard);
            logger.info("MapController.doRealname:token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if ("success".equals(result)) {
                return ResultUtils.genResult(ResultUtils.RESULT_STATUS_SUCCESS,"实名认证成功");
            } else {
                return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, result);
            }
        } catch (Exception e) {
            logger.error("查询用户实名认证失败：token={},e={}", token, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

}
