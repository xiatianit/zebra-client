package com.zebra.zebraclient.biz.pojo.bean;

/**
 * 
 * @author owen
 *
 */
public class UserToken {

    private String  deviceId;

    private Integer deviceType;

    private Integer time;

    private Long    uid;

    private String  nickName;

    private String  tokenStr;

    private String  currentReceiveId;

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

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTokenStr() {
        return tokenStr;
    }

    public void setTokenStr(String tokenStr) {
        this.tokenStr = tokenStr;
    }

}
