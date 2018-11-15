package com.zebra.zebraclient.biz.service;

import java.util.Map;

import com.zebra.zebraclient.biz.pojo.bean.UserToken;

public interface PayCenterService {

	/**
	 * 支付宝订单数据加签
	 * 
	 * @param orderId
	 * @param money
	 * @param orderName
	 * @return
	 */

	Map<String, String> alipayOrderSign(String orderId, Double money, String orderName);

	/**
	 * 微信订单数据加签
	 * 
	 * @param orderId
	 * @param money
	 * @param orderName
	 * @param remoteAddrIP
	 * @return
	 */
	Map<String, String> weixinOrderSign(String orderId, Double money, String orderName, String remoteAddrIP);

	/**
	 * 支付宝异步通知方法
	 * 
	 * @param money
	 * @param outTradeNo
	 * 
	 */

	String alipayNotify(String notifyId, String tradeStatus, String outTradeNo, Double money,
			Map<String, String> params);

	/**
	 * 根据订单ID获取用户订单状态
	 * 
	 * @param orderId
	 * @return
	 */
	Integer getUserOrderPayStatus(String orderId);

	/**
	 * 修改用户订单支付状态
	 * 
	 * @param orderId
	 *            订单ID
	 * @param payType
	 *            支付类型
	 * @param money
	 *            支付金额
	 * @param payStatus
	 *            支付状态
	 * @return
	 */
	void editUserOrder(String orderId, Integer payType, Double money, Integer payStatus);

	/**
	 * 押金订单业务处理
	 * 
	 * @param userToken
	 * @param payType
	 */
	Map<String, String> depositOrder(UserToken userToken, Integer payType, Double money, String orderName,
			String spbillCreateIP);

	/**
	 * 普通订单业务处理
	 * 
	 * @param payType
	 * @param orderId
	 * @param money
	 * @param orderName
	 * @param spbillCreateIP
	 * @return
	 */

	Map<String, String> commonOrder(Integer payType, String orderId, Double money, String orderName,
			String spbillCreateIP);

	/**
	 * 微信仿真测试获取API秘钥
	 * 
	 * @return
	 */
	Map<String, String> getsignkey();

	/**
	 * 获取用户押金状态
	 * 
	 * @param uid
	 * @return
	 */
	Integer getUserWalletStatus(Long uid);

	/**
	 * 更新用户押金状态
	 * 
	 * @param uid
	 * @param depositOrderId
	 * @param payType
	 * @param money
	 * @param depositStatus
	 */
	void editUserWalletStatus(Long uid, String depositOrderId, Integer payType, Double money, Integer depositStatus);

	/**
	 * 小程序押金订单
	 * 
	 * @param userToken
	 * @param payType
	 * @param money
	 * @param orderName
	 * @param spbillCreateIP
	 * @param openId
	 * @return
	 */
	Map<String, String> miniProgramDepositOrder(UserToken userToken, Integer payType, Double money, String orderName,
			String spbillCreateIP, String openId);

	/**
	 * 小程序普通订单
	 * 
	 * @param payType
	 * @param orderId
	 * @param money
	 * @param orderName
	 * @param spbillCreateIP
	 * @param openId
	 * @return
	 */
	Map<String, String> miniProgramCommonOrder(Integer payType, String orderId, Double money, String orderName,
			String spbillCreateIP, String openId);

	/**
	 * 小程序订单加签
	 * 
	 * @param orderId
	 * @param money
	 * @param orderName
	 * @param spbillCreateIP
	 * @param openId
	 * @return
	 */
	Map<String, String> miniProgramOrderSign(String orderId, Double money, String orderName, String spbillCreateIP,
			String openId);
/**
 * 支付宝退款接口
 * @param userToken
 * @param orderId
 * @param money
 * @return
 */
	Map<String, Object> aliOrderRefund(UserToken userToken,String orderId,Double money);
	/**
	 * 微信退款接口
	 * @param userToken
	 * @param orderId
	 * @param money
	 * @param refundSource
	 * @return
	 */
	Map<String,String> weixinOrderRefund(UserToken userToken,String orderId,Double money,String refundSource);
	
}
