package com.zebra.zebraclient.dal.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zebra.zebraclient.dal.dataobject.BikeDO;
import com.zebra.zebraclient.dal.dataobject.businessObject.BikeBO;

/**
 * @author owen
 */
public interface BikeMapper {

    /***************** 具体的业务逻辑 *****************/

    /**
     * 获取附近的车辆信息
     *
     * @param bikeBO 数据操作对象
     * @return List<BikeDO>
     */
    List<BikeDO> selectNearBikeByCondition(BikeBO bikeBO);


    /**
     * 获取车子对象
     * @param bikeCode 车子编码
     * @return 车
     */
    BikeDO selectByPk(@Param("bikeCode") String bikeCode);

    /**
     * 获取车子对象
     * @param bikeNo 车子NO
     * @return 车
     */
    BikeDO selectByBikeNo(@Param("bikeNo") String bikeNo);


    /**
     * 更新
     *
     * @param bikeDO
     * @return
     */
    int update(BikeDO bikeDO);
}
