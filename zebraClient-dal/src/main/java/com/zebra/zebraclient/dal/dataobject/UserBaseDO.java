package com.zebra.zebraclient.dal.dataobject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

import lombok.Data;

/**
 * 用户基本信息DO
 * @author owen
 *
 */
@Data
public class UserBaseDO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Long uid;
    private String phone;
    private String password;
    private String headPortrait;
    private String nickName;
    private Integer sex;
    private Integer age;
    private String job;
    private String income;
    private Integer verifyStatus;
    private String actualName;
    private String idCardNo;
    private Integer userStatus;
    private Integer registerTime;
    private Integer modifyTime;
    private Integer creditScore;
    private String inviteCode;
    private String invitedCode;
    
    
    /*** 添加扩展的东西 ****/
    /** 累计行程 单位：千米 保留两位小数*/
    private Double totalDistance;
    
    /**节约用时 单位：分钟 */
    private Integer saveTime;
    
    /**节约支出 单位：元 保留2位小数*/
    private Double savePrice;
    
    
    public Long getUid() {
        return uid;
    }


    public void setUid(Long uid) {
        this.uid = uid;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getHeadPortrait() {
        return headPortrait;
    }


    public void setHeadPortrait(String headPortrait) {
        this.headPortrait = headPortrait;
    }


    public String getNickName() {
        return nickName;
    }


    public void setNickName(String nickName) {
        this.nickName = nickName;
    }


    public Integer getSex() {
        return sex;
    }


    public void setSex(Integer sex) {
        this.sex = sex;
    }


    public Integer getAge() {
        return age;
    }


    public void setAge(Integer age) {
        this.age = age;
    }


    public String getJob() {
        return job;
    }


    public void setJob(String job) {
        this.job = job;
    }


    public String getIncome() {
        return income;
    }


    public void setIncome(String income) {
        this.income = income;
    }


    public Integer getVerifyStatus() {
        return verifyStatus;
    }


    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }


    public String getActualName() {
        return actualName;
    }


    public void setActualName(String actualName) {
        this.actualName = actualName;
    }


    public String getIdCardNo() {
        return idCardNo;
    }


    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }


    public Integer getUserStatus() {
        return userStatus;
    }


    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }


    public Integer getRegisterTime() {
        return registerTime;
    }


    public void setRegisterTime(Integer registerTime) {
        this.registerTime = registerTime;
    }


    public Integer getModifyTime() {
        return modifyTime;
    }


    public void setModifyTime(Integer modifyTime) {
        this.modifyTime = modifyTime;
    }


    public Integer getCreditScore() {
        return creditScore;
    }


    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }


    public String getInviteCode() {
        return inviteCode;
    }


    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }


    public String getInvitedCode() {
        return invitedCode;
    }


    public void setInvitedCode(String invitedCode) {
        this.invitedCode = invitedCode;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
