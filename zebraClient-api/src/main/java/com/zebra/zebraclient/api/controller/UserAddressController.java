package com.zebra.zebraclient.api.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.pojo.converter.UserAddressConverter;
import com.zebra.zebraclient.biz.pojo.viewobject.UserAddressVO;
import com.zebra.zebraclient.biz.pojo.viewobject.UserDetailVO;
import com.zebra.zebraclient.biz.service.UserAddressService;
import com.zebra.zebraclient.dal.dataobject.UserAddressDO;

/**
 * 用户基本信息controller
 * 
 * @author owen
 */
@RestController
@RequestMapping("/userAddress")
public class UserAddressController {

    private final static Logger logger = LoggerFactory.getLogger(UserAddressController.class);

    @Resource
    private RabbitTemplate      rabbitTemplate;

    @Autowired
    private TokenHelper         tokenHelper;

    @Autowired
    private UserAddressService  userAddressService;

    /**
     * 获取用户地址信息
     * 
     * @param token
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public Result<UserDetailVO> doGetUserAddressInfo(@RequestParam("token") String token) {
        logger.info("获取用户明地址细信息：token={}", token);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("UserAddressController.doGetUserAddressInfo={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            UserAddressDO userAddressDO = new UserAddressDO(userToken.getUid());
            List<UserAddressDO> listDO = userAddressService.queryUserAddressByCondition(userAddressDO);
            int size = listDO.size();
            UserAddressVO userAddressDOHome = new UserAddressVO(0);
            UserAddressVO userAddressDOCompany = new UserAddressVO(0);
            if (listDO != null && size > 0) {
                UserAddressDO temp1 = listDO.get(0);
                if (temp1.getAddressType().intValue() == ZebraConstant.USERADDRESS_ADDRESSTYPE_HOME) {
                    userAddressDOHome = UserAddressConverter.userDO2UserVO(temp1);
                    if (size > 1) {
                        UserAddressDO temp2 = listDO.get(1);
                        userAddressDOCompany = UserAddressConverter.userDO2UserVO(temp2);
                    }
                } else {
                    userAddressDOCompany = UserAddressConverter.userDO2UserVO(temp1);
                    if (size > 1) {
                        UserAddressDO temp2 = listDO.get(1);
                        userAddressDOHome = UserAddressConverter.userDO2UserVO(temp2);
                    }
                }
            }
            return ResultUtils.genResultWithSuccess("home", userAddressDOHome, "campony", userAddressDOCompany);
        } catch (Exception e) {
            logger.error("获取用户地址明细信息失败,token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 修改用户地址
     * 
     * @param token
     * @param sex
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doEditUserAddress(@RequestParam("token") String token, @RequestParam("addressType") Integer addressType,
            @RequestParam("latitude") BigDecimal latitude, @RequestParam("longitude") BigDecimal longitude, @RequestParam("address") String address,
            @RequestParam("subAddress") String subAddress, @RequestParam(value = "poiId", required = false) String poiId) {
        logger.info("修改用户地址：token={}，addressType={}，latitude={},longitude={},address={},subAddress={},poiId={}", token, addressType, latitude,
                longitude, address, subAddress, poiId);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("UserController.doGetUserInfo:token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            UserAddressDO userAddressDO = new UserAddressDO(null, userToken.getUid(), addressType, latitude, longitude, address, subAddress, poiId);
            userAddressService.addUserAddress(userAddressDO);
            return ResultUtils.genResultWithSuccess();
        } catch (Exception e) {
            logger.error("修改用户地址失败,token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 修改用户地址
     * 
     * @param token
     * @param sex
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> dodDeleteUserAddress(@RequestParam("token") String token, @RequestParam("addressType") Integer addressType) {
        logger.info("删除用户地址：token={}，addressType={}", token, addressType);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("UserController.doGetUserInfo:token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            userAddressService.deleteUserAddressByAddTypeAndUid(userToken.getUid(), addressType);
            return ResultUtils.genResultWithSuccess();
        } catch (Exception e) {
            logger.error("删除用户地址失败,token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

}
