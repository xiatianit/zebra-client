package com.zebra.zebraclient.dal.dataobject;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 终端设备DO--手机 设备终端信息表（uid、token、device_id）
 * 
 * @author owen
 *
 */
public class DeviceDO extends PageDO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** 手机终端设备唯一标示号 */
    private String            deviceId;

    private Long              uid;

    /** 设备类型(1、安卓 2、IOS) */
    private Integer           deviceType;

    private String            token;

    /** app版本 */
    private String            appVersion;

    /** 当前终端接送ID */
    private String            currentReceiveId;

    /*** 最后一次更新时间 */
    private Integer           lastUpdateTime;

    public String getCurrentReceiveId() {
        return currentReceiveId;
    }

    public void setCurrentReceiveId(String currentReceiveId) {
        this.currentReceiveId = currentReceiveId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Integer getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Integer lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
