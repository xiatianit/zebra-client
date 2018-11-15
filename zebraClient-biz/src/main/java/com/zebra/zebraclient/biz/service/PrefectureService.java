package com.zebra.zebraclient.biz.service;

import java.util.List;

import com.zebra.zebraclient.dal.dataobject.CityDO;

/**
 * 行政区域管理
 * 
 * @author owen
 *
 */
public interface PrefectureService {

    /**
     * 获取所有城市信息列表
     * 
     * @return
     */
    List<CityDO> queryCicyByCondition();

}
