package com.zebra.zebraclient.api.controller;

import java.util.List;

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
import com.zebra.zebraclient.biz.pojo.converter.UserOrderConverter;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderDetailVO;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderUnComplatedVO;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderVO;
import com.zebra.zebraclient.biz.service.UserOrderService;
import com.zebra.zebraclient.dal.dataobject.UserOrderDO;

/**
 * 订单相关controlellr
 * 
 * @author owen
 */
@RestController
@RequestMapping("/orderapi")
public class OrderController {

    private final static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private TokenHelper         tokenHelper;

    @Autowired
    private UserOrderService    userOrderService;

    /**
     * 获取订单列表
     * 
     * @param tokene
     * @return
     */
    @RequestMapping(value = "/orderList", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> queryOrderList(@RequestParam(value = "token", required = true) String token) {
        logger.info("获取订单列表：token={}", token);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("OrderController.queryOrderList={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            List<UserOrderDO> list = userOrderService.queryUserOrderByUid(userToken.getUid());
            List<OrderVO> listVO = UserOrderConverter.userOrderDO2OrderVO(list);
            return ResultUtils.genResultWithSuccess("orderList", listVO);
        } catch (Exception e) {
            logger.error("获取订单列表：token={},e={}", token, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 获取订单详情
     * 
     * @param tokene
     * @return
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> queryOrderDetail( @RequestParam(value = "token", required = true) String token,
            @RequestParam(value = "orderId", required = true) String orderId) {
  
        logger.info("获取订单列表：token={}", token);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("OrderController.queryOrderDetail={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            OrderDetailVO orderDetailVO = userOrderService.queryUserOrderDetailByOrderId(orderId);
            return ResultUtils.genResultWithSuccess(orderDetailVO);
        } catch (Exception e) {
            logger.error("获取订单列表：token={},e={}", token, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 获取订单详情
     * 
     * @param token
     * @return
     */
    @RequestMapping(value = "/currentOrder", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> queryCurrentOrder(@RequestParam(value = "token", required = true) String token) {
        logger.info("获取订单列表：token={}", token);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("OrderController.queryCurrentOrder,token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            OrderUnComplatedVO orderUnComplatedVO = userOrderService.doJudgeUsersOrderSituation(userToken.getUid());
            logger.info("OrderController.queryCurrentOrder,orderUnComplatedVO={}", token, JsonUtil.getJsonFromObject(orderUnComplatedVO));
            return ResultUtils.genResultWithSuccess(orderUnComplatedVO);
        } catch (Exception e) {
            logger.error("获取订单列表：token={},e={}", token, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

}
