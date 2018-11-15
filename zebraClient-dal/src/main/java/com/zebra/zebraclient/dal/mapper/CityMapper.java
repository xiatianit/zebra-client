package com.zebra.zebraclient.dal.mapper;

import java.util.List;

import com.zebra.zebraclient.dal.dataobject.CityDO;

public interface CityMapper {

    /**
     * 获取所有的城市接口
     * @return
     */
    List<CityDO> selectByCondition();

}
