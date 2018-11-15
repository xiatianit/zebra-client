package com.zebra.zebraclient.biz.pojo.viewobject;

import lombok.Data;

/**
 * 试图层用户显示
 * 
 * @author owen
 *
 */
@Data
public class UserDetailVO {

    private Long    uid;

    /** 用户权限 备用字段 暂无用 */
    private Integer role;

    private Integer sex;

    /** 信用分 */
    private Integer creditScore;

    private String  userName;

    private String  phone;

    /** 头像地址 */
    private String  headPortrait;

    private String  nickName;

    /** 实名认证 0未认证 1已认证 2认证中 */
    private Integer verifyStatus;

    /** 累计行程 单位：千米 保留两位小数 */
    private Double  totalDistance;

    /** 节约用时 单位：分钟 */
    private Integer saveTime;

    /** 节约支出 单位：元 保留1位小数 */
    private Double  savePrice;

    /** 1:未交押金 2:已交押金 3:退还押金中 */
    private Integer depositState;

}
