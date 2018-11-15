package com.zebra.zebraclient.biz.pojo;

import java.io.UnsupportedEncodingException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.zebra.pony.util.StringUtil;

/**
 * 动态参数配置
 * 
 * @author owen
 */
@ConfigurationProperties(prefix = "business.param")
@Component
public class DynamicParamConfig {

    /** 用户token失效时间 24 * 60 * 60 */
    private int    userTokenEffectiveTime         = 86400;

    /** 用户发送手机验证码失效时间 15 * 60 */
    private int    userLoginAuthCodeEffectiveTime = 900;

    /** 用户获取附近车辆半径的距离 单位:米 线上时，需要改动 TODO */
    private int    userSearchNearBikeRadius       = 5000;

    /** 电池满格，可跑40公里 单位:千米 */
    private Double batteryFullMils                = 40.00;

    /** 电车电池电量低于5时，则给予提示 */
    private int    bikeBatteryElecQuanlityLow     = 5;

    /** 电车计费价格描述 最低消费一元钱 */
    private String priceDesc                      = "0.1元/分钟";

    private Double pricePerMin                    = 0.1;

    /** 最低消费金额 */
    private Double lowestCost                     = 1.0;

    /** 押金金额 */
    private int    disposit                       = 299;

    /** 用户锁车附近距离 */
    private int    userLockNearSiteRadius         = 999999999;

    /* -----------------------支付宝配置参数 begin------------------ */
    /* 收款支付宝用户ID， 如果该值为空，则默认为商户签约账号对应的支付宝用户ID */
    private String alipaySellerId                 = "2088611342523812";
    /* 订单交易描述 */
    private String alipayBody                     = "斑马出行";
    /* 订单标题 */
    private String alipaySubject                  = "斑马出行";
    /* 未付款支付宝交易的超时时间 */
    private String alipayTimeoutExpress           = "30m";
    /* 接收异步通知的URL */
    private String alipayNotifyUrl                = "https://userappapi2.banmabike.com/zebraclientapi/payapi/alipayNotifyURL";
    // /* -----------------------支付宝配置参数 end------------------*/
    // /* -----------------------微信配置参数 begin------------------*/
    private String wxNotifyUrl                    = "https://userappapi2.banmabike.com/zebraclientapi/payapi/weixinNotifyURL";

    public int getUserTokenEffectiveTime() {
        return userTokenEffectiveTime;
    }

    public void setUserTokenEffectiveTime(int userTokenEffectiveTime) {
        this.userTokenEffectiveTime = userTokenEffectiveTime;
    }

    public int getUserLoginAuthCodeEffectiveTime() {
        return userLoginAuthCodeEffectiveTime;
    }

    public void setUserLoginAuthCodeEffectiveTime(int userLoginAuthCodeEffectiveTime) {
        this.userLoginAuthCodeEffectiveTime = userLoginAuthCodeEffectiveTime;
    }

    public int getUserSearchNearBikeRadius() {
        return userSearchNearBikeRadius;
    }

    public void setUserSearchNearBikeRadius(int userSearchNearBikeRadius) {
        this.userSearchNearBikeRadius = userSearchNearBikeRadius;
    }

    public Double getBatteryFullMils() {
        return batteryFullMils;
    }

    public void setBatteryFullMils(Double batteryFullMils) {
        this.batteryFullMils = batteryFullMils;
    }

    public int getBikeBatteryElecQuanlityLow() {
        return bikeBatteryElecQuanlityLow;
    }

    public void setBikeBatteryElecQuanlityLow(int bikeBatteryElecQuanlityLow) {
        this.bikeBatteryElecQuanlityLow = bikeBatteryElecQuanlityLow;
    }

    public String getPriceDesc() {
        if (!StringUtil.isBlank(priceDesc)) {
            try {
                return new String(priceDesc.getBytes("iso-8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return priceDesc;
    }

    public int getUserLockNearSiteRadius() {
        return userLockNearSiteRadius;
    }

    public void setUserLockNearSiteRadius(int userLockNearSiteRadius) {
        this.userLockNearSiteRadius = userLockNearSiteRadius;
    }

    public void setPriceDesc(String priceDesc) {
        this.priceDesc = priceDesc;
    }

    public Double getPricePerMin() {
        return pricePerMin;
    }

    public void setPricePerMin(Double pricePerMin) {
        this.pricePerMin = pricePerMin;
    }

    public Double getLowestCost() {
        return lowestCost;
    }

    public void setLowestCost(Double lowestCost) {
        this.lowestCost = lowestCost;
    }

    public int getDisposit() {
        return disposit;
    }

    public void setDisposit(int disposit) {
        this.disposit = disposit;
    }

    public String getAlipaySellerId() {
        return alipaySellerId;
    }

    public void setAlipaySellerId(String alipaySellerId) {
        this.alipaySellerId = alipaySellerId;
    }

    public String getAlipayBody() {
        return alipayBody;
    }

    public void setAlipayBody(String alipayBody) {
        this.alipayBody = alipayBody;
    }

    public String getAlipaySubject() {
        return alipaySubject;
    }

    public void setAlipaySubject(String alipaySubject) {
        this.alipaySubject = alipaySubject;
    }

    public String getAlipayTimeoutExpress() {
        return alipayTimeoutExpress;
    }

    public void setAlipayTimeoutExpress(String alipayTimeoutExpress) {
        this.alipayTimeoutExpress = alipayTimeoutExpress;
    }

    public String getAlipayNotifyUrl() {
        return alipayNotifyUrl;
    }

    public void setAlipayNotifyUrl(String alipayNotifyUrl) {
        this.alipayNotifyUrl = alipayNotifyUrl;
    }

    public String getWxNotifyUrl() {
        return wxNotifyUrl;
    }

    public void setWxNotifyUrl(String wxNotifyUrl) {
        this.wxNotifyUrl = wxNotifyUrl;
    }

}
