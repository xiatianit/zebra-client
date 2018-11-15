package com.zebra.zebraclient.biz.service.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.zebra.common.constant.ZebraConstant;
import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.biz.exception.ZebraclientException;
import com.zebra.zebraclient.biz.pojo.DynamicParamConfig;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.service.PayCenterService;
import com.zebra.zebraclient.biz.service.UserOrderService;
import com.zebra.zebraclient.biz.service.WalletService;
import com.zebra.zebraclient.biz.util.AlipayUtil;
import com.zebra.zebraclient.biz.util.CollectionUtil;
import com.zebra.zebraclient.biz.util.DatetimeUtil;
import com.zebra.zebraclient.biz.util.HttpUtils;
import com.zebra.zebraclient.biz.util.PayUtil;
import com.zebra.zebraclient.biz.util.XmlUtil;
import com.zebra.zebraclient.dal.dataobject.UserOrderDO;
import com.zebra.zebraclient.dal.dataobject.WalletDO;

/**
 * 押金信息
 *
 * @author owen
 */
@Service
public class PayCenterServiceImpl implements PayCenterService {

    private final static Logger logger = LoggerFactory.getLogger(PayCenterServiceImpl.class);

    @Autowired
    private UserOrderService userOrderService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private DynamicParamConfig dynamicParamConfig;

    /**
     * 支付宝订单数据加签
     */
    @Override
    public Map<String, String> alipayOrderSign(String orderId, Double money, String orderName) {
        logger.info("支付宝对订单进行加签,orderId={},money={},orderName={}", orderId, money, orderName);
        String orderInfo = "";
        Map<String, String> param = new HashMap<String, String>();
        // 公共请求参数封装
        param.put("app_id", ZebraConstant.APPID);// appid
        param.put("method", ZebraConstant.METHOD);// 接口名称
        param.put("format", AlipayConstants.FORMAT_JSON);// json
        param.put("charset", AlipayConstants.CHARSET_UTF8);// 编码格式
        param.put("timestamp", DatetimeUtil.formatDateTime(new Date()));
        param.put("version", "1.0");
        param.put("notify_url", dynamicParamConfig.getAlipayNotifyUrl()); // 支付宝服务器异步通知商户地址
        param.put("sign_type", AlipayConstants.SIGN_TYPE_RSA2);// 使用RSA2进行加密

        Map<String, Object> pcont = new HashMap<String, Object>();
        // 支付业务请求参数封装
        pcont.put("out_trade_no", orderId); // 订单号
        pcont.put("total_amount", String.valueOf(money));// 交易金额
        pcont.put("subject", orderName); // 订单标题
        pcont.put("body", dynamicParamConfig.getAlipayBody());// 对交易的描述
        pcont.put("timeout_express", dynamicParamConfig.getAlipayTimeoutExpress());// 未付款支付宝交易的超时时间这里设置为30分钟
        pcont.put("seller_id", dynamicParamConfig.getAlipaySellerId()); // 收款支付宝用户ID,如果该值为空，则默认为商户签约账号对应的支付宝用户ID
        pcont.put("product_code", ZebraConstant.PRODUCT_CODE);// 销售产品码
        String bizcontentJson = JsonUtil.getJsonFromObject(pcont);
        param.put("biz_content", bizcontentJson); // 业务请求参数json格式化
        logger.info("1.请求参数按照key=value&key=value方式拼接的未签名原始字符串param={}", JsonUtil.getJsonFromObject(param));
        try {
            param.put("sign", PayUtil.getSign(param, AlipayUtil.APP_PRIVATE_KEY)); // 对支付参数进行加签
            logger.info("2.加签之后的字符串数据,parm={}", JsonUtil.getJsonFromObject(param));
            orderInfo = PayUtil.getSignEncodeUrl(param, true); // 对订单信息进行编码
            logger.info(
                    "3.最后对请求字符串的所有一级value（biz_content作为一个value）进行encode，编码格式按请求串中的charset为准，没传charset按UTF-8处理，获得最终的请求字符串,orderInfo={}",
                    orderInfo);
        } catch (Exception e) {
            logger.error("支付宝加签异常,e={}", e);
            e.printStackTrace();
        }
        Map<String, String> orderInfoMap = new HashMap<String, String>();
        orderInfoMap.put("fullString", orderInfo);
        orderInfoMap.put("mark", "0");
        logger.info("支付宝加签完成，返回的加签订单信息,orderInfoMap={}", JsonUtil.getJsonFromObject(orderInfoMap));
        return orderInfoMap;
    }

    /*
     * 微信支付订单加签
     */
    @Override
    public Map<String, String> weixinOrderSign(String orderId, Double money, String orderName, String spbillCreateIP) {
        logger.info("微信订单加签,orderId={},money={},spbillCreateIP={}", orderId, money, spbillCreateIP);
        Map<String, String> restmap = null;
        try {
            // 参数封装
            Map<String, String> param = new HashMap<String, String>();
            // BigDecimal转字符串并去除末位的0
            String total_fee = BigDecimal.valueOf(money).multiply(BigDecimal.valueOf(100)).stripTrailingZeros()
                    .toPlainString();
            param.put("appid", ZebraConstant.WXAPPID);// 微信开放平台应用APPID
            param.put("mch_id", ZebraConstant.WXMCH_ID);// 微信支付分配的商户号
            param.put("device_info", "WEB");// 终端设备号(门店号或收银设备ID)，默认请传"WEB"
            param.put("nonce_str", PayUtil.getNonceStr());// 随机字符串，不长于32位
            param.put("body", orderName);// 商品交易描述
            param.put("attach", "斑马出行");// 自定义数据
            param.put("out_trade_no", orderId);// 订单ID
            param.put("total_fee", total_fee);// 总费用
            param.put("spbill_create_ip", spbillCreateIP);// 终端IP
            param.put("notify_url", dynamicParamConfig.getWxNotifyUrl());// 微信回调地址
            param.put("trade_type", "APP");// 交易类型
            param.put("sign", PayUtil.getWXSign(param, ZebraConstant.API_SECRET));
            System.out.println("组装的参数：" + param);
            logger.info("调用微信订单加签接口，组装好的参数，param={}", param);
            String restxml = HttpUtils.post(ZebraConstant.ORDER_PAY, XmlUtil.xmlFormat(param, false));

            // XML格式数据转换成map对象
            restmap = XmlUtil.xmlParse(restxml);
            logger.info("调微信统一下单API生成预付单，获取预付单信息,restmap={}", JsonUtil.getJsonFromObject(restmap));
        } catch (Exception e) {
            logger.error("调用微信统一下单API失败,e={},", e);
            e.printStackTrace();
        }
        // 组装返回客户端加签数据
        Map<String, String> payMap = new HashMap<String, String>();
        if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
            payMap.put("appid", ZebraConstant.WXAPPID);
            payMap.put("partnerid", ZebraConstant.WXMCH_ID);
            payMap.put("prepayid", restmap.get("prepay_id"));
            payMap.put("package", "Sign=WXPay");
            payMap.put("noncestr", PayUtil.getNonceStr());
            payMap.put("timestamp", PayUtil.payTimestamp());
            try {
                payMap.put("sign", PayUtil.getWXSign(payMap, ZebraConstant.API_SECRET));
                payMap.put("mark", "0");
                logger.info("微信返回带签名的客户端信息payMap={}", payMap);
                return payMap;
            } catch (Exception e) {
                logger.error("微信订单信息加签失败,e={}", e);
                e.printStackTrace();
            }
        } else {
            payMap.put("mark", "1");
            return payMap;
        }
        return null;
    }

    /* 支付宝回调方法 */
    @Override
    public String alipayNotify(String notifyId, String tradeStatus, String outTradeNo, Double money,
                               Map<String, String> params) {
        logger.info("支付宝异步回调方法，notifyId={},tradeStatus={},outTradeNo={},money={},params={}", notifyId, tradeStatus,
                outTradeNo, money, params);
        /*
         * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
		 * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
		 * 3、校验通知中的seller_id或者seller_email)是否为out_trade_no这笔单据的对应的操作方（有的时候，
		 * 一个商户可能有多个seller_id/seller_email），
		 * 4、验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
		 * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。在支付宝的业务通知中，
		 * 只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
		 */
        if (notifyId != "" && notifyId != null) {
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                boolean signVerified = false;
                try {
                    signVerified = AlipaySignature.rsaCheckV1(params, AlipayUtil.ALIPAY_PUBLIC_KEY,
                            AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2);
                } catch (AlipayApiException e) {
                    logger.error("支付宝验签异常e={}", e);
                    e.printStackTrace();
                } // 校验签名是否正确
                if (signVerified) {
                    logger.info("支付宝校验签名成功，tradeStatus={},outTradeNo={},money={}", tradeStatus, outTradeNo, money);
                    // 验签成功后,先判断该订单是押金类型还是普通类型，如果是押金类型，则修改押金状态为已支付押金，否则修改用户订单信息
                    // 按照支付结果异步通知中的描述，对支付结果中的业务内容进行1\2\3\4二次校验，校验成功后在response中返回success，校验失败返回failure
                    if (outTradeNo.substring(0, 7).equals("DEPOSIT")) {
                        logger.info("该订单类型是押金订单，outTradeNo={}", outTradeNo);
                        Long uid = Long.parseLong(outTradeNo.substring(24));
                        if (getUserWalletStatus(uid) == ZebraConstant.WALLET_DEPOSIT_STATUS_1) {
                            editUserWalletStatus(uid, outTradeNo, ZebraConstant.USER_ORDER_PAY_TYPE_ALIPAY, money,
                                    ZebraConstant.WALLET_DEPOSIT_STATUS_2);
                            return "success";
                        }

                    } else {
                        if (getUserOrderPayStatus(outTradeNo).equals(ZebraConstant.USER_ORDER_PAY_STATUS_NO)) {
                            logger.info("修改用户订单信息状态，tradeStatus={},outTradeNo={},money={}", tradeStatus, outTradeNo,
                                    money);
                            editUserOrder(outTradeNo, ZebraConstant.USER_ORDER_PAY_TYPE_ALIPAY, money,
                                    ZebraConstant.USER_ORDER_PAY_STATUS_YES);
                            return "success";
                        }
                    }
                    return "success";
                } else {
                    // 验签失败则记录异常日志，并在response中返回failure.
                    logger.info("支付宝校验签名不通过,notifyId={},tradeStatus={},outTradeNo={},money={},params={}", notifyId,
                            tradeStatus, outTradeNo, money, params);
                    return "failure";
                }
            }
        }
        return "failure";
    }

    /**
     * 获取用户订单状态
     */
    @Override
    public Integer getUserOrderPayStatus(String orderId) {
        logger.info("获取用户订单状态，orderId={}", orderId);
        if (orderId != null) {
            UserOrderDO userOrderDO = userOrderService.queryUserOrderByPk(orderId);
            return userOrderDO.getPayStatus();
        } else {
            return null;
        }
    }

    /**
     * 修改用户订单支付状态
     */
    @Override
    public void editUserOrder(String orderId, Integer payType, Double money, Integer payStatus) {
        logger.info("更新用户订单状态，orderId={},payType={},money={},payStatus={}", orderId, payType, money, payStatus);
        UserOrderDO userOrderDO = new UserOrderDO();
        userOrderDO.setOrderId(orderId);
        userOrderDO.setPayType(payType);
        userOrderDO.setPayAmount(money);
        userOrderDO.setPayStatus(payStatus);
        userOrderService.editUserOrder(userOrderDO);

    }

    /**
     * 订单类型为押金业务处理
     */
    @Override
    public Map<String, String> depositOrder(UserToken userToken, Integer payType, Double money, String orderName,
                                            String spbillCreateIP) {
        logger.info("订单类型是押金支付，调押金订单处理方法depositOrder");
        WalletDO wallet = walletService.queryWalletByPk(userToken.getUid());
        String depositOrderId = PayUtil.getTempOrderId() + userToken.getUid();
        WalletDO walletDO = new WalletDO();
        if (wallet == null) {
            walletDO.setUid(userToken.getUid());
            walletDO.setDepositOrderId(depositOrderId);
            walletDO.setDepositSource(payType);
            walletDO.setDepositPayType(payType);
            walletDO.setDepositAmount(new BigDecimal(money));
            walletDO.setDepositStatus(ZebraConstant.WALLET_DEPOSIT_STATUS_1);
            walletService.addWallet(walletDO);
        } else if (wallet != null && wallet.getDepositStatus() == ZebraConstant.WALLET_DEPOSIT_STATUS_1) {
            editUserWalletStatus(userToken.getUid(), depositOrderId, payType, money,
                    ZebraConstant.WALLET_DEPOSIT_STATUS_1);
        } else if (wallet.getDepositStatus() == ZebraConstant.WALLET_DEPOSIT_STATUS_2) {
            Map<String, String> msg = new HashMap<String, String>();
            msg.put("mark", "2");
            return msg;
        }
        if (payType.equals(ZebraConstant.USER_ORDER_PAY_TYPE_ALIPAY)) {
            logger.info("支付类型是支付宝调支付宝加签方法，depositOrderId={},money={},orderName={}", depositOrderId, money, orderName);
            return alipayOrderSign(depositOrderId, money, orderName);
        } else if (payType.equals(ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT)) {
            logger.info("支付类型是微信支付调微信加签方法，depositOrderId={},money={},orderName={},spbillCreateIP={}", depositOrderId,
                    money, orderName, spbillCreateIP);
            return weixinOrderSign(depositOrderId, money, orderName, spbillCreateIP);
        }
        return null;
    }

    /**
     * 获取用户押金状态
     *
     * @param uid
     * @return
     */
    @Override
    public Integer getUserWalletStatus(Long uid) {
        logger.info("获取用户押金状态，uid={}", uid);
        return walletService.doGetWalletDepositStatus(uid);
    }

    /**
     * 更新用户押金状态
     *
     * @param uid
     * @param depositOrderId
     * @param payType
     * @param money
     * @param depositStatus
     */
    @Override
    public void editUserWalletStatus(Long uid, String depositOrderId, Integer payType, Double money,
                                     Integer depositStatus) {
        logger.info("更新用户押金状态，uid={},depositOrderId={},payType={},money={},depositStatus={}", uid, depositOrderId,
                payType, money, depositStatus);
        WalletDO walletDO = new WalletDO();
        walletDO.setUid(uid);
        walletDO.setDepositOrderId(depositOrderId);
        walletDO.setDepositSource(payType);
        walletDO.setDepositPayType(payType);
        walletDO.setDepositAmount(new BigDecimal(money));
        walletDO.setDepositStatus(depositStatus);
        walletService.editWallet(walletDO);
    }

    /**
     * 订单类型为普通类型
     */
    @Override
    public Map<String, String> commonOrder(Integer payType, String orderId, Double money, String orderName,
                                           String spbillCreateIP) {
        Integer status = getUserOrderPayStatus(orderId);
        if (status == ZebraConstant.USER_ORDER_PAY_STATUS_NO) {
            logger.info("订单类型是普通支付，调普通订单处理方法commonOrder");
            if (payType.equals(ZebraConstant.USER_ORDER_PAY_TYPE_ALIPAY)) {
                logger.info("支付类型是支付宝调支付宝加签方法,payType={},orderId={},money={},orderName={}", payType, orderId, money,
                        orderName);
                return alipayOrderSign(orderId, money, orderName);
            } else if (payType.equals(ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT)) {
                logger.info("支付类型是微信支付调微信加签方法,payType={},orderId={},money={},orderName={},spbillCreateIP={}", payType,
                        orderId, money, orderName, spbillCreateIP);
                return weixinOrderSign(orderId, money, orderName, spbillCreateIP);
            }
        } else if (status == ZebraConstant.USER_ORDER_PAY_STATUS_YES) {
            Map<String, String> msg = new HashMap<String, String>();
            msg.put("mark", "2");
            return msg;
        }
        return null;

    }

    @Override
    public Map<String, String> getsignkey() {
        Map<String, String> pa = new HashMap<String, String>();
        pa.put("mch_id", ZebraConstant.WXMCH_ID);// 微信支付分配的商户号
        pa.put("nonce_str", PayUtil.getNonceStr());// 随机字符串，不长于32位
        try {
            pa.put("sign", PayUtil.getWXSign(pa, ZebraConstant.API_SECRET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("组装的参数：" + pa);
        logger.info("获取微信沙盒加签key");
        String restxml = HttpUtils.post("https://api.mch.weixin.qq.com/sandboxnew/pay/getsignkey",
                XmlUtil.xmlFormat(pa, false));
        // XML格式数据转换成map对象
        System.out.println("微信返回信息：" + restxml);
        Map<String, String> tempMap = new HashMap<String, String>();
        tempMap.put("restxml", restxml);
        return tempMap;
    }

    /**
     * 小程序押金订单
     */
    @Override
    public Map<String, String> miniProgramDepositOrder(UserToken userToken, Integer payType, Double money,
                                                       String orderName, String spbillCreateIP, String openId) {
        logger.info("小程序--订单类型是押金支付，调押金订单处理方法miniProgramDepositOrder");
        try {
            String depositOrderId = PayUtil.getTempOrderId() + userToken.getUid();
            WalletDO walletDO = new WalletDO();
            WalletDO wallet = walletService.queryWalletByPk(userToken.getUid());
            if (wallet == null) {
                walletDO.setUid(userToken.getUid());
                walletDO.setDepositOrderId(depositOrderId);
                walletDO.setDepositSource(payType);
                walletDO.setDepositPayType(payType);
                walletDO.setDepositAmount(new BigDecimal(money));
                walletDO.setDepositStatus(ZebraConstant.WALLET_DEPOSIT_STATUS_1);
                walletService.addWallet(walletDO);
            } else if (wallet != null && wallet.getDepositStatus() == ZebraConstant.WALLET_DEPOSIT_STATUS_1) {
                editUserWalletStatus(userToken.getUid(), depositOrderId, payType, money,
                        ZebraConstant.WALLET_DEPOSIT_STATUS_1);
            } else if (wallet.getDepositStatus() == ZebraConstant.WALLET_DEPOSIT_STATUS_2) {
                Map<String, String> msg = new HashMap<String, String>();
                msg.put("mark", "2");
                return msg;
            }
            if (payType.equals(ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT)) {
                logger.info(
                        "小程序--支付类型是微信支付调微信加签方法，payType={},depositOrderId={},money={},orderName={},spbillCreateIP={},openId={}",
                        payType, depositOrderId, money, orderName, spbillCreateIP, openId);
                return miniProgramOrderSign(depositOrderId, money, orderName, spbillCreateIP, openId);
            }
        } catch (Exception e) {
            logger.info("小程序押金支付异常，e={}", e.getMessage());
            throw new ZebraclientException("小程序押金支付异常");
        }
        return null;
    }

    /**
     * 小程序普通订单
     */
    @Override
    public Map<String, String> miniProgramCommonOrder(Integer payType, String orderId, Double money, String orderName,
                                                      String spbillCreateIP, String openId) {
        Integer status = getUserOrderPayStatus(orderId);
        if (status == ZebraConstant.USER_ORDER_PAY_STATUS_NO) {
            logger.info("小程序--订单类型是普通支付，调普通订单处理方法miniProgramCommonOrder");
            if (payType.equals(ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT)) {
                logger.info(
                        "小程序--支付类型是微信支付调微信加签方法,payType={},orderId={},money={},orderName={},spbillCreateIP={},openId={}",
                        payType, orderId, money, orderName, spbillCreateIP, openId);
                return miniProgramOrderSign(orderId, money, orderName, spbillCreateIP, openId);
            }
        } else if (status == ZebraConstant.USER_ORDER_PAY_STATUS_YES) {
            Map<String, String> msg = new HashMap<String, String>();
            msg.put("mark", "2");
            return msg;
        }
        return null;
    }

    /**
     * 小程序订单加签
     */
    @Override
    public Map<String, String> miniProgramOrderSign(String orderId, Double money, String orderName,
                                                    String spbillCreateIP, String openId) {
        logger.info("小程序订单加签,orderId={},money={},orderName={},spbillCreateIP={}，openId={}", orderId, money, orderName,
                spbillCreateIP, openId);
        Map<String, String> restmap = null;
        try {
            // 参数封装
            Map<String, String> param = new HashMap<String, String>();
            // BigDecimal转字符串并去除末位的0
            String total_fee = BigDecimal.valueOf(money).multiply(BigDecimal.valueOf(100)).stripTrailingZeros()
                    .toPlainString();
            param.put("appid", ZebraConstant.MINIAPPID);// 小程序APPID
            param.put("mch_id", ZebraConstant.WXMCH_ID);// 微信支付分配的商户号
            param.put("device_info", "WEB");// 终端设备号(门店号或收银设备ID)，默认请传"WEB"
            param.put("nonce_str", PayUtil.getNonceStr());// 随机字符串，不长于32位
            param.put("body", orderName);// 商品交易描述
            param.put("attach", "斑马出行");// 自定义数据
            param.put("out_trade_no", orderId);// 订单ID
            param.put("total_fee", total_fee);// 总费用
            param.put("spbill_create_ip", spbillCreateIP);// 终端IP
            param.put("notify_url", dynamicParamConfig.getWxNotifyUrl());// 微信回调地址
            param.put("trade_type", "JSAPI");// 交易类型
            param.put("openid", openId);
            param.put("sign", PayUtil.getWXSign(param, ZebraConstant.API_SECRET));
            System.out.println("组装的参数：" + param);
            logger.info("小程序调用微信订单加签接口，组装好的参数，param={}", param);
            String restxml = HttpUtils.post(ZebraConstant.ORDER_PAY, XmlUtil.xmlFormat(param, false));

            // XML格式数据转换成map对象
            restmap = XmlUtil.xmlParse(restxml);
            logger.info("小程序调微信统一下单API生成预付单，获取预付单信息,restmap={}", JsonUtil.getJsonFromObject(restmap));
        } catch (Exception e) {
            logger.error("小程序调用微信统一下单API失败,e={},", e);
            e.printStackTrace();
        }
        // 组装返回客户端加签数据
        Map<String, String> payMap = new HashMap<String, String>();
        if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
            payMap.put("appId", ZebraConstant.MINIAPPID);
            payMap.put("package", "prepay_id=" + restmap.get("prepay_id"));
            payMap.put("signType", "MD5");
            payMap.put("nonceStr", restmap.get("nonce_str"));
            payMap.put("timeStamp", PayUtil.payTimestamp());
            try {
                payMap.put("sign", PayUtil.getWXSign(payMap, ZebraConstant.API_SECRET));
                payMap.put("mark", "0");
                logger.info("返回给小程序二次签名的信息payMap={}", payMap);
                return payMap;
            } catch (Exception e) {
                payMap.put("mark", "1");
                logger.error("小程序微信订单信息加签失败,e={}", e);
                return payMap;
            }
        } else {
            payMap.put("mark", "1");
            return payMap;
        }
    }

    /**
     * 支付宝退款业务
     */
    @Override
    public Map<String, Object> aliOrderRefund(UserToken userToken, String orderId, Double money) {
        logger.info("押金来源是支付宝，调支付宝退款接口，orderId={},money={}", orderId, money);
        Map<String, Object> restmap = new HashMap<>();// 返回支付宝退款信息
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest(); // 统一收单交易退款接口
        // 只需要传入业务参数
        Map<String, Object> param = new HashMap<>();
        param.put("out_trade_no", orderId); // 商户订单号
        param.put("refund_amount", money);// 退款金额
        param.put("refund_reason", "斑马电车押金退还");// 退款原因
        param.put("out_request_no", PayUtil.getRefundNo()); // 退款流水号
        alipayRequest.setBizContent(JsonUtil.getJsonFromObject(param));
        try {
            AlipayTradeRefundResponse alipayResponse = AlipayUtil.getAlipayClient().execute(alipayRequest);
            if (alipayResponse.isSuccess()) {
                // 调用成功，则处理业务逻辑
                if ("10000".equals(alipayResponse.getCode())) {
                    logger.info("调用支付宝退款接口成功，处理业务逻辑，订单号orderId={}", alipayResponse.getOutTradeNo());
                    restmap.put("out_trade_no", alipayResponse.getOutTradeNo());
                    restmap.put("trade_no", alipayResponse.getTradeNo());
                    restmap.put("buyer_logon_id", alipayResponse.getBuyerLogonId());// 用户的登录id
                    restmap.put("gmt_refund_pay", alipayResponse.getGmtRefundPay()); // 退款支付时间
                    restmap.put("buyer_user_id", alipayResponse.getBuyerUserId());// 买家在支付宝的用户id
                    if (null != userToken) {
                        logger.info("该订单是押金订单，修改用户押金状态为未支付，orderId={},uid={}", orderId, userToken.getUid());
                        editUserWalletStatus(userToken.getUid(), orderId, ZebraConstant.USER_ORDER_PAY_TYPE_ALIPAY, money,
                                ZebraConstant.WALLET_DEPOSIT_STATUS_1);
                    }
                    restmap.put("mark", "0");
                    return restmap;
                }
            } else {
                restmap.put("mark", "1");
                logger.info("押金退还失败：" + alipayResponse.getMsg() + ":" + alipayResponse.getSubMsg());
                return restmap;
            }
        } catch (AlipayApiException e) {
            logger.error("订单退款失败,调支付宝退款接口发生异常,e={}", e.getMessage());
            e.printStackTrace();
        }
        return restmap;
    }

    /**
     * 微信退款业务
     */
    @Override
    public Map<String, String> weixinOrderRefund(UserToken userToken, String orderId, Double money, String miniFlag) {
        logger.info("押金退款调微信退款接口，orderId={},money={},miniFlag={}", orderId, money, miniFlag);
        Map<String, String> restmap = null;
        try {
            Map<String, String> parm = new HashMap<String, String>();
            if (miniFlag != null && miniFlag.equals("miniFlag")) {
                parm.put("appid", ZebraConstant.MINIAPPID);
            } else {
                parm.put("appid", ZebraConstant.WXAPPID);
            }
            parm.put("mch_id", ZebraConstant.WXMCH_ID);
            parm.put("nonce_str", PayUtil.getNonceStr());
            parm.put("out_trade_no", orderId);// 订单号
            parm.put("out_refund_no", PayUtil.getRefundNo()); // 退款单号
            parm.put("total_fee",
                    BigDecimal.valueOf(money).multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()); // 订单总金额
            parm.put("refund_fee",
                    BigDecimal.valueOf(money).multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()); // 退款金额
            parm.put("op_user_id", ZebraConstant.WXMCH_ID);
            parm.put("refund_account", "REFUND_SOURCE_RECHARGE_FUNDS");// 退款方式
            parm.put("sign", PayUtil.getWXSign(parm, ZebraConstant.API_SECRET));
            String restxml = HttpUtils.posts(ZebraConstant.ORDER_REFUND, XmlUtil.xmlFormat(parm, false));
            restmap = XmlUtil.xmlParse(restxml);
        } catch (Exception e) {
            logger.error("调微信退款接口失败", e);
            restmap.put("mark", "1");
            return restmap;
        }
        Map<String, String> refundMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
            logger.info("微信押金退款成功,订单号:" + restmap.get("out_trade_no") + "押金退款成功，商户退款单号:" + restmap.get("out_refund_no")
                    + "，微信退款单号:" + restmap.get("refund_id"));
            refundMap.put("transaction_id", restmap.get("transaction_id"));
            refundMap.put("out_trade_no", restmap.get("out_trade_no"));
            refundMap.put("refund_id", restmap.get("refund_id"));
            refundMap.put("out_refund_no", restmap.get("out_refund_no"));
            if (null != userToken) {
                logger.info("该订单是押金订单，修改用户押金状态为未支付，orderId={},uid={}", orderId, userToken.getUid());
                editUserWalletStatus(userToken.getUid(), orderId, ZebraConstant.USER_ORDER_PAY_TYPE_WECHAT, money,
                        ZebraConstant.WALLET_DEPOSIT_STATUS_1);
            }
            restmap.put("mark", "0");
            return restmap;
        } else {
            logger.info("微信订单退款失败：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
            restmap.put("mark", "1");
            return restmap;
        }
    }
}
