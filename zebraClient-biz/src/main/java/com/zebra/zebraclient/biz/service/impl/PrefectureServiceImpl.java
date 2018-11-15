package com.zebra.zebraclient.biz.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zebra.zebraclient.biz.service.PrefectureService;
import com.zebra.zebraclient.dal.dataobject.CityDO;
import com.zebra.zebraclient.dal.mapper.CityMapper;

/**
 * 
 * @author owen
 *
 */
@Service
public class PrefectureServiceImpl implements PrefectureService {

    @Autowired
    private CityMapper slaveCityMapper;

    /**
     * 获取城市列表信息
     */
    @Override
    public List<CityDO> queryCicyByCondition() {
        return slaveCityMapper.selectByCondition();
    }

}
