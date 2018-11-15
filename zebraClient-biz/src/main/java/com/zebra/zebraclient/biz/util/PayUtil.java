package com.zebra.zebraclient.biz.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.api.AlipayConstants;

/**
 * 
 * 
 * @author xia
 * @version 2.2
 */

public class PayUtil {

	/**
	 * 订单类型是押金则先生成临时订单号
	 * 
	 * @return
	 */
	public static String getTempOrderId() {
		
		return "DEPOSIT" + DatetimeUtil.formatDate(new Date(), DatetimeUtil.TIME_STAMP_PATTERN);
	}

	/**
	 * 退款单号
	 * 
	 * @return
	 */
	public static String getRefundNo() {
		// 自增8位数 00000001
		return "RNO" + DatetimeUtil.formatDate(new Date(), DatetimeUtil.TIME_STAMP_PATTERN) + "00000001";
	}

	/**
	 * 交易单号
	 * 
	 * @return
	 */
	public static String getTransferNo() {
		// 自增8位数 00000001
		return "TNO" + DatetimeUtil.formatDate(new Date(), DatetimeUtil.TIME_STAMP_PATTERN) + "00000001";
	}

	/**
	 * 返回客户端ip
	 * 
	 * @param request
	 * @return
	 *//*
	public static String getRemoteAddrIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtil.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = ip.indexOf(",");
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if (StringUtil.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return request.getRemoteAddr();
	}

	*//**
	 * 获取服务器的ip地址
	 * 
	 * @param request
	 * @return
	 *//*
	public static String getLocalIp(HttpServletRequest request) {
		return request.getLocalAddr();
	}

	*/
	/**
	 * 创建支付随机字符串32位
	 * 
	 * @return
	 */
	public static String getNonceStr() {
		return RandomUtil.randomString(RandomUtil.LETTER_NUMBER_CHAR, 32);
	}

	/**
	 * 支付时间戳
	 * 
	 * @return
	 */
	public static String payTimestamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

	/**
	 * 返回签名编码拼接url
	 * 
	 * @param params
	 * @param isEncode
	 * @return
	 */
	public static String getSignEncodeUrl(Map<String, String> map, boolean isEncode) {
		String sign = map.get("sign");
		String encodedSign = "";
		if (CollectionUtil.isNotEmpty(map)) {
			map.remove("sign");
			List<String> keys = new ArrayList<String>(map.keySet());
			// key排序
			Collections.sort(keys);

			StringBuilder authInfo = new StringBuilder();

			boolean first = true;// 是否第一个
			for (String key: keys) {
				if (first) {
					first = false;
				} else {
					authInfo.append("&");
				}
				authInfo.append(key).append("=");
				if (isEncode) {
					try {
						authInfo.append(URLEncoder.encode(map.get(key), AlipayConstants.CHARSET_UTF8));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else {
					authInfo.append(map.get(key));
				}
			}

			try {
				encodedSign = authInfo.toString() + "&sign=" + URLEncoder.encode(sign, AlipayConstants.CHARSET_UTF8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return encodedSign.replaceAll("\\+", "%20");
	}

	/**
	 * 支付宝对支付参数信息进行签名
	 * 
	 * @param map
	 *            待签名授权信息
	 * 
	 * @return
	 */
	public static String getSign(Map<String, String> map, String rsaKey) {
		List<String> keys = new ArrayList<String>(map.keySet());
		// key排序
		Collections.sort(keys);

		StringBuilder authInfo = new StringBuilder();
		boolean first = true;
		for (String key : keys) {
			if (first) {
				first = false;
			} else {
				authInfo.append("&");
			}
			authInfo.append(key).append("=").append(map.get(key)); 
		}

		return SignUtils.sign(authInfo.toString(), rsaKey);
	}
/**
 * 微信支付对参数进行加签
 * @param params  需要签名的参数
 * @param paternerKey  API密码
 * @return
 * @throws UnsupportedEncodingException
 */
	public static String getWXSign(Map<String, String> params, String paternerKey)throws UnsupportedEncodingException{
		return MD5Utils.getMD5(createWXSign(params, false) + "&key=" + paternerKey).toUpperCase();
	}
	
	/**
	 * 微信构造签名参数
	 * @param params
	 * @param encode
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String createWXSign(Map<String, String> params, boolean encode) throws UnsupportedEncodingException{
		Set<String> keysSet = params.keySet();
		Object[] keys = keysSet.toArray();
		Arrays.sort(keys);
		StringBuffer temp = new StringBuffer();
		boolean first = true;
		for (Object key : keys) {
			if (key == null || StringUtil.isEmpty(params.get(key))) // 参数为空不参与签名
				continue;
			if (first) {
				first = false;
			} else {
				temp.append("&");
			}
			temp.append(key).append("=");
			Object value = params.get(key);
			String valueStr = "";
			if (null != value) {
				valueStr = value.toString();
			}
			if (encode) {
				temp.append(URLEncoder.encode(valueStr, "UTF-8"));
			} else {
				temp.append(valueStr);
			}
		}
		return temp.toString();
	}
}
