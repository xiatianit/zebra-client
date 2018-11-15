package com.zebra.zebraclient.biz.util;

import java.util.Random;

import com.zebra.common.coder.convertor.DouHex;

/**
 * 随即数功能
 * 
 * @author owen
 *
 */
public class RandomUtil {
	
	public static final String ALL_CHAR = "-_#&$@+-*/%()[]0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final String LETTER_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final String NUMBER_CHAR = "0123456789";

	public static final String SPECIAL_CHAR = "-_#&$@+-*/%()[]";
	
	public static final String LETTER_NUMBER_CHAR = LETTER_CHAR + NUMBER_CHAR;
	
    /**
     * 产生4位随机数(0000-9999)
     * 
     * @return 4位随机数
     */
    public static String getFourRandom() {
        Random random = new Random();
        String fourRandom = random.nextInt(10000) + "";
        int randLength = fourRandom.length();
        if (randLength < 4) {
            for (int i = 1; i <= 4 - randLength; i++)
                fourRandom = "0" + fourRandom;
        }
        return fourRandom;
    }

    public static void main(String[] args) {

        int now = (int) (System.currentTimeMillis() / 1000);
        int time2015 = 1420041600; // 2015-01-01
        int d = (int) 1e3;
        long sn = (long) (now - time2015) * d + (int) (Math.random() * d);
        String generateInviteCode = DouHex.toDouHexString(sn);
        System.out.println(generateInviteCode);

    }
    
	

	/**
	 * 返回一个定长的随机字符串
	 * 
	 * @param chars
	 *            模型串
	 * @param length
	 *            随机长度
	 * @return
	 */
	public static String randomString(String chars, int length) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	/**
	 * 返回一个定长的随机字符串字母全部大写
	 * 
	 * @param chars
	 *            模型串
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String randomLowerString(String chars, int length) {
		return randomString(chars, length).toLowerCase();
	}

	/**
	 * 返回一个定长的随机字符串字母全部小写
	 * 
	 * @param chars
	 *            模型串
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String randomUpperString(String chars, int length) {
		return randomString(chars, length).toLowerCase();
	}

	/**
	 * 生成一个定长的纯0字符串
	 * 
	 * @param length
	 *            字符串长度
	 * @return 纯0字符串
	 */
	public static String randomZeroString(int length) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			sb.append("0");
		}
		return sb.toString();
	}
    
}
