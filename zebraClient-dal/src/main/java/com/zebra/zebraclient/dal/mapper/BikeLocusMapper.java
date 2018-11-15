package com.zebra.zebraclient.dal.mapper;

import java.util.List;

import com.zebra.zebraclient.dal.dataobject.BikeLocusDO;
import com.zebra.zebraclient.dal.dataobject.businessObject.BikeLocusBO;

public interface BikeLocusMapper {

    /**
     * 通过
     * 
     * @param bikeLocusDO
     * @return
     */
    List<BikeLocusBO> selectByCondition(BikeLocusDO bikeLocusDO);

}
