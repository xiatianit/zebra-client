package com.zebra.zebraclient.biz.enums;

public enum ResultErrorEnum {

    LOGIN_AUTHCODE_EFFECTIVE(101, "手机登录验证码失效"), 
    LOGIN_AUTHCODE_COMPARE_ERROR(102, "手机登录验证码比对失败"),
    DEVICE_INPUT_ERROR(103, "设备号输入有误"),
    USER_TOKEN_INVALID(104, "用户token无效"),
    APP_VERSION(105,"当前APP版本太低，请更新到最新APP使用");
    

    private Integer errorCode;
    private String  errorMsg;

    ResultErrorEnum(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
