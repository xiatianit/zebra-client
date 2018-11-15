package com.zebra.zebraclient.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.zebra.pony.common.model.Result;
import com.zebra.pony.common.utils.ResultUtils;
import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.api.helper.TokenHelper;
import com.zebra.zebraclient.biz.enums.ResultErrorEnum;
import com.zebra.zebraclient.biz.pojo.DynamicParamConfig;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.service.WalletService;

/**
 * 用户基本信息controller
 * 
 * @author owen
 */
@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final static Logger logger = LoggerFactory.getLogger(WalletController.class);

    @Autowired
    private TokenHelper         tokenHelper;

    @Autowired
    private WalletService       walletService;

    @Autowired
    private DynamicParamConfig  dynamicParamConfig;

    /**
     * 获取用户押金状态
     *
     * @param token
     * @return
     */
    @RequestMapping("/depositstate")
    @ResponseBody
    public Result<?> doGetDepositstate(@RequestParam("token") String token) {
        logger.info("获取用户押金状态：token={}", token);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("WalletController.doGetDepositstate={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            int walletDepositStatus = walletService.doGetWalletDepositStatus(userToken.getUid());
            return ResultUtils.genResultWithSuccess("state", walletDepositStatus, "deposit", dynamicParamConfig.getDisposit());
        } catch (Exception e) {
            logger.error("获取用户押金状态失败,token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }


}
