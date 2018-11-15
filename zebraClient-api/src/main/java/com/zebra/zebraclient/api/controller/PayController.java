package com.zebra.zebraclient.api.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.zebra.zebraclient.biz.service.PayCenterService;
import com.zebra.zebraclient.biz.service.WalletService;
import com.zebra.zebraclient.biz.util.CollectionUtil;
import com.zebra.zebraclient.biz.util.FileUtil;
import com.zebra.zebraclient.biz.util.HttpUtils;
import com.zebra.zebraclient.biz.util.PayUtil;
import com.zebra.zebraclient.biz.util.XmlUtil;
import com.zebra.zebraclient.dal.dataobject.WalletDO;

/**
 * 支付相关的controller
 *
 * @author xia
 */
@RestController
@RequestMapping("/payapi")
public class PayController {

    private final static Logger logger = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private PayCenterService payCenterService;

    @Autowired
    private WalletService walletService;

    /**
     * APP订单支付
     *
     * @param token
     * @param payType
     * @param orderId
     * @param money
     * @return
     */
    @RequestMapping(value = "/fetchPaymentDetail", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> orderSign(@RequestParam(value = "token", required = true) String token,
                               @RequestParam(value = "payType", required = true) Integer payType,
                               @RequestParam(value = "orderId", required = true) String orderId,
                               @RequestParam(value = "orderType", required = true) String orderType,
                               @RequestParam(value = "money", required = true) Double money,
                               @RequestParam(value = "orderName", required = true) String orderName,
                               @RequestParam(value = "app_version", required = true) String app_version,
                               @RequestParam(value = "spbillCreateIP", required = true) String spbillCreateIP) {
        logger.info("APP调订单支付接口获取订单加签信息：token={},app_version={},payType={},orderId={},orderType={},money={}", token, app_version, payType, orderId,
                orderType, money);
        try {
            String appVersion[] = app_version.split("\\.");
            if (Integer.parseInt(appVersion[0]) < 2) {
                return ResultUtils.genResult(ResultErrorEnum.APP_VERSION.getErrorCode(), ResultErrorEnum.APP_VERSION.getErrorMsg());
            }
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("PayController.fetchPaymentDetail,userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            if (orderType.equals(ZebraConstant.USER_ORDER_TYPE_YAJIN)) {
                // 押金订单
                logger.info("订单类型为押金支付类型：orderType={},payType={},money={},orderName={},spbillCreateIP={}", orderType,
                        payType, money, orderName, spbillCreateIP);
                return ResultUtils.genResultWithSuccess(
                        payCenterService.depositOrder(userToken, payType, money, orderName, spbillCreateIP));
            } else if (orderType.equals(ZebraConstant.USER_ORDER_TYPE_PUTONG)) {
                // 普通订单
                logger.info("订单类型为普通支付类型：orderType={},orderId={},payType={},money={},orderName={},spbillCreateIP={}",
                        orderType, orderId, payType, money, orderName, spbillCreateIP);
                return ResultUtils.genResultWithSuccess(
                        payCenterService.commonOrder(payType, orderId, money, orderName, spbillCreateIP));
            }
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, "订单类型参数异常,该订单即不是普通订单1,也不是押金订单2");
        } catch (Exception e) {
            logger.error("订单支付信息异常：token={},payType={},orderId{}，money={},e={}", token, payType, orderId, money, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 小程序支付接口
     *
     * @param token
     * @param payType
     * @param orderId
     * @param orderType
     * @param money
     * @param orderName
     * @param spbillCreateIP
     * @param openId
     * @return
     */
    @RequestMapping(value = "/miniProgramPay", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> miniProgramPay(@RequestParam(value = "token", required = true) String token,
                                    @RequestParam(value = "payType", required = true) Integer payType,
                                    @RequestParam(value = "orderId", required = true) String orderId,
                                    @RequestParam(value = "orderType", required = true) String orderType,
                                    @RequestParam(value = "money", required = true) Double money,
                                    @RequestParam(value = "orderName", required = true) String orderName,
                                    @RequestParam(value = "spbillCreateIP", required = true) String spbillCreateIP,
                                    @RequestParam(value = "openId", required = true) String openId) {
        logger.info(
                "小程序--调订单支付接口获取订单加签信息PayController.miniProgramPay,payType={},orderId={},orderType={},money={},orderName={},spbillCreateIP={},openId={}",
                payType, orderId, orderType, money, orderName, spbillCreateIP);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("小程序--调订单支付接口校验token,userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            if (orderType.equals(ZebraConstant.USER_ORDER_TYPE_YAJIN)) {
                // 押金订单
                logger.info("订单类型为押金支付类型：orderType={},payType={},money={},orderName={},spbillCreateIP={},openId={}",
                        orderType, payType, money, orderName, spbillCreateIP, openId);
                return ResultUtils.genResultWithSuccess(payCenterService.miniProgramDepositOrder(userToken, payType,
                        money, orderName, spbillCreateIP, openId));
            } else if (orderType.equals(ZebraConstant.USER_ORDER_TYPE_PUTONG)) {
                // 普通订单
                logger.info(
                        "订单类型为普通支付类型：orderType={},orderId={},payType={},money={},orderName={},spbillCreateIP={},openId={}",
                        orderType, orderId, payType, money, orderName, spbillCreateIP, openId);
                return ResultUtils.genResultWithSuccess(payCenterService.miniProgramCommonOrder(payType, orderId, money,
                        orderName, spbillCreateIP, openId));
            }
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, "订单类型参数异常,该订单即不是普通订单1,也不是押金订单2");
        } catch (Exception e) {
            logger.error("订单支付信息异常：token={},payType={},orderId{}，money={},e={}", token, payType, orderId, money, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }

    }

    /**
     * 获取微信沙箱测试API秘钥
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/getsignkey", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> getsignkey(HttpServletRequest request, HttpServletResponse response) {
        return ResultUtils.genResultWithSuccess(payCenterService.getsignkey());
    }

    /**
     * 支付宝异步回调接口
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/alipayNotifyURL", method = RequestMethod.POST)
    public void alipayNotifyURL(HttpServletRequest request, HttpServletResponse response) {
        logger.info("支付宝异步通知接口alipayNotifyURL");
        // 获取支付宝返回参数
        Map<String, String> params = getReqParams(request);
        // 异步通知ID
        String notifyId = request.getParameter("notify_id");
        // 订单号
        String orderId = request.getParameter("out_trade_no");
        // 订单金额
        Double totalAmount = Double.parseDouble(request.getParameter("total_amount"));
        // 交易状态码
        String tradeStatus = request.getParameter("trade_status");
        String result = payCenterService.alipayNotify(notifyId, tradeStatus, orderId, totalAmount, params);
        // 返回给支付宝结果
        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            logger.error("订单支付异步回调异常：e={}", e);
        }
    }

    /**
     * 支付宝返回参数解析
     *
     * @param request
     * @return
     */
    public Map<String, String> getReqParams(HttpServletRequest request) {
        Enumeration<?> pNames = request.getParameterNames();
        Map<String, String> params = new HashMap<String, String>();
        while (pNames.hasMoreElements()) {
            String pName = (String) pNames.nextElement();
            params.put(pName, request.getParameter(pName));
        }
        return params;
    }

    /**
     * 微信支付异步调用接口
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/weixinNotifyURL", method = RequestMethod.POST)
    public void weixinNotifyURL(HttpServletRequest request, HttpServletResponse response) {
        logger.info("微信异步通知回调接口weixinNotifyURL");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");
        ServletInputStream in = null;
        try {
            in = request.getInputStream();
            String resxml = FileUtil.readInputStream2String(in);
            Map<String, String> restmap = XmlUtil.xmlParse(resxml);
            logger.info("微信支付结果异步通知结果,restmap={}", JsonUtil.getJsonFromObject(restmap));
            if (restmap != null && !restmap.equals("")) {
                if ("SUCCESS".equals(restmap.get("return_code"))) {
                    // 订单支付成功业务处理
                    String orderId = restmap.get("out_trade_no");
                    // 将单位为分转换成元
                    Double money = new BigDecimal(restmap.get("total_fee")).divide(BigDecimal.valueOf(100))
                            .doubleValue();

                    if (orderId.substring(0, 7).equals("DEPOSIT")) {
                        logger.info("押金支付订单,orderId={}", orderId);
                        Long uid = Long.parseLong(orderId.substring(24));
                        if (payCenterService.getUserWalletStatus(uid) == ZebraConstant.WALLET_DEPOSIT_STATUS_1) {
                            String sign = restmap.get("sign");// 微信返回的签名
                            restmap.remove("sign");
                            // 生成新的签名
                            String signnow = PayUtil.getWXSign(restmap, ZebraConstant.API_SECRET);
                            // 比较新生成的签名和微信返回来的签名是否一致
                            if (signnow.equals(sign)) {
                                logger.info("微信签名验证成功,修改押金支付状态为已交押金,orderId={},payType={},money={}", orderId,
                                        ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT, money);
                                payCenterService.editUserWalletStatus(uid, orderId,
                                        ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT, money,
                                        ZebraConstant.WALLET_DEPOSIT_STATUS_2);
                                // 处理成功后相应给响应xml
                                Map<String, String> respMap = new HashMap<String, String>();
                                respMap.put("return_code", "SUCCESS"); // 响应给微信服务器
                                respMap.put("return_msg", "OK");
                                String resXml = XmlUtil.xmlFormat(restmap, true);
                                response.getWriter().write(resXml);
                                in.close();
                            } else {
                                logger.info("微信签名验证失败,sign={},signnow={}", sign, signnow);
                            }
                        }
                    } else {
                        logger.info("普通支付订单,orderId={}", orderId);
                        // 通过商户订单判断是否该订单已经处理 如果处理跳过 如果未处理先校验sign签名 再进行订单业务相关的处理
                        if (payCenterService.getUserOrderPayStatus(orderId)
                                .equals(ZebraConstant.USER_ORDER_PAY_STATUS_NO)) {
                            String sign = restmap.get("sign");// 微信返回的签名
                            restmap.remove("sign");
                            // 生成新的签名
                            String signnow = PayUtil.getWXSign(restmap, ZebraConstant.API_SECRET);
                            // 比较新生成的签名和微信返回来的签名是否一致
                            if (signnow.equals(sign)) {
                                logger.info("微信签名验证成功,修改用户订单支付状态为已支付,orderId={},payType={},money={},payStatus={}",
                                        orderId, ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT, money,
                                        ZebraConstant.USER_ORDER_PAY_STATUS_YES);
                                payCenterService.editUserOrder(orderId, ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT, money,
                                        ZebraConstant.USER_ORDER_PAY_STATUS_YES);
                                // 处理成功后相应给响应xml
                                Map<String, String> respMap = new HashMap<String, String>();
                                respMap.put("return_code", "SUCCESS"); // 响应给微信服务器
                                respMap.put("return_msg", "OK");
                                String resXml = XmlUtil.xmlFormat(restmap, true);
                                response.getWriter().write(resXml);
                                in.close();
                            } else {
                                logger.info("普通支付订单，微信签名验证失败，sign={},signnow={}", sign, signnow);
                            }
                        }
                    }
                }
            } else {
                in.close();
                logger.info("订单支付通知：支付失败，" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
            }
        } catch (Exception e) {
            logger.error("订单支付通知：支付失败，e={}", e);
            e.printStackTrace();
        }
    }

    /**
     * 押金退款接口
     *
     * @return
     */
    @RequestMapping(value = "/returndeposit", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> returndeposit(@RequestParam(value = "token", required = true) String token,
                                   @RequestParam(value = "miniFlag", required = false) String miniFlag) {
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("押金退款接口PayController.returndeposit,userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            WalletDO walletDO = walletService.queryWalletByPk(userToken.getUid());
            if (walletDO != null) {
                Integer walletStatus = walletDO.getDepositStatus();
                if (walletStatus != null && walletStatus == ZebraConstant.WALLET_DEPOSIT_STATUS_2) {
                    if (walletDO.getDepositPayType() == ZebraConstant.USER_ORDER_PAY_TYPE_ALIPAY) {
                        return ResultUtils.genResultWithSuccess(payCenterService.aliOrderRefund(userToken,
                                walletDO.getDepositOrderId(), walletDO.getDepositAmount().doubleValue()));
                    } else {
                        return ResultUtils.genResultWithSuccess(payCenterService.weixinOrderRefund(userToken,
                                walletDO.getDepositOrderId(), walletDO.getDepositAmount().doubleValue(), miniFlag));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("押金退款失败，token={}", token);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
        return null;
    }

    /**
     * 退款接口
     *
     * @return
     */
    @RequestMapping(value = "/return/money", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> returnMoney(@RequestParam(value = "orderId", required = true) String orderId,
                                 @RequestParam(value = "payType", required = true) Integer payType,
                                 @RequestParam(value = "money", required = true) Double money,
                                 @RequestParam(value = "miniFlag", required = false) String miniFlag) {
        try {
            logger.info("退款接口PayController.returnmoney,orderId={}", orderId);
            if (orderId == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "订单Id为空");
            }
            if (payType == ZebraConstant.USER_ORDER_PAY_TYPE_ALIPAY) {
                return ResultUtils.genResultWithSuccess(payCenterService.aliOrderRefund(null,
                        orderId, money));
            } else {
                return ResultUtils.genResultWithSuccess(payCenterService.weixinOrderRefund(null,
                        orderId, money, miniFlag));
            }
        } catch (Exception e) {
            logger.error("退款失败，orderId={},payType={},money={}", orderId, payType, money);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 订单查询
     *
     * @param request
     * @param response
     * @param transactionId
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/orderPayQuery", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> orderPayQuery(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(value = "transactionId", required = true) String transactionId,
                                   @RequestParam(value = "orderId", required = true) String orderId) {
        Map<String, String> restmap = null;
        try {
            Map<String, String> parm = new HashMap<String, String>();
            parm.put("appid", ZebraConstant.WXAPPID);
            parm.put("mch_id", ZebraConstant.WXMCH_ID);
            parm.put("transaction_id", transactionId);
            parm.put("out_trade_no", orderId);
            parm.put("nonce_str", PayUtil.getNonceStr());
            parm.put("sign", PayUtil.getSign(parm, ZebraConstant.API_SECRET));
            String restxml = HttpUtils.post(ZebraConstant.ORDER_PAY_QUERY, XmlUtil.xmlFormat(parm, false));
            restmap = XmlUtil.xmlParse(restxml);
            if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
                // 订单查询成功 处理业务逻辑
                logger.info("订单查询：订单" + restmap.get("out_trade_no") + "支付成功");
                return ResultUtils.genResultWithSuccess();
            }
        } catch (Exception e) {
            logger.error("订单查询：支付失败：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());

        }
        return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, "订单查询失败");

    }

}
