package com.zebra.zebraclient.dal.dataobject;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 单车定价表DO
 * 
 * @ClassName: BikeChargePriceDO
 * @Description: by CodeGenerate
 * @author 
 * 
 */
@Data
@ToString
public class BikeChargePriceDO implements Serializable {

	/** 
	 * 主键 ( 主键 ) 
	 */
	private Long id;
	/** 
	 * 车型编号  
	 */
	private String bikeModelCode;
	/** 
	 * 车型名称  
	 */
	private String bikeModelName;
	/** 
	 * 单价（元）  
	 */
	private Double unitPrice;
	/** 
	 * 最小单元10分钟，15分钟，20分钟  
	 */
	private Integer unit;
	/** 
	 * 所属加盟商编号  
	 */
	private String franchiserCode;
	/** 
	 * 所属加盟商名称  
	 */
	private String franchiserName;
}