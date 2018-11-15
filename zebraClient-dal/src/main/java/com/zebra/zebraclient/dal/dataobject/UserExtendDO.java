package com.zebra.zebraclient.dal.dataobject;

import java.io.Serializable;

import lombok.Data;

/**
 * 用户扩展信息
 * 
 * @author owen
 *
 */
@Data
public class UserExtendDO implements Serializable {

    // 总累计用时：
    // 总节约用时(单位分钟)：60(分钟/小时) * ［累计行驶里程（公里） / 自行车匀速10（公里/小时）］ - 累计用时（分钟）
    // 总节约支出(单位公里)：2（元/公里） * 累计行驶里程（公里） - 累计费用（元）

    /**
     * 
     */
    private static final long serialVersionUID = 8857412801351388964L;

    private Long              uid;

    /** 累计行程 单位：千米 保留两位小数 */
    private Double            totalDistance;

    /** 节约用时 单位：分钟 */
    private Integer           saveTime;

    /** 节约支出 单位：元 保留2位小数 */
    private Double            savePrice;
   /** 累计总费用 单位：元 保留2位小数*/
    private Double    totalFee;
   /** 累计骑行时间 单位：分钟*/
    private Integer totalRidetime;

}
