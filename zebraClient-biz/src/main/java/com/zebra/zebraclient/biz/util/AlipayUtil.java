package com.zebra.zebraclient.biz.util;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.zebra.common.constant.ZebraConstant;

/**
 * 
 * @author xia
 *
 */

public class AlipayUtil {


	public static String APP_PRIVATE_KEY = ZebraConstant.APP_PRIVATE_KEY; // app支付私钥

	public static String ALIPAY_PUBLIC_KEY = ZebraConstant.ALIPAY_PUBLIC_KEY; // 支付宝公钥

	

	// 统一收单交易创建接口
	private static AlipayClient alipayClient = null;

	public static AlipayClient getAlipayClient() {
		if (alipayClient == null) {
			synchronized (AlipayUtil.class) {
				if (null == alipayClient) {
					alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", ZebraConstant.APPID,
							APP_PRIVATE_KEY, AlipayConstants.FORMAT_JSON, AlipayConstants.CHARSET_UTF8,
							ALIPAY_PUBLIC_KEY,"RSA2");
				}
			}
		}
		return alipayClient;
	}
}
