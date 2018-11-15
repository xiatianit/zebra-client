package com.zebra.zebraclient.biz.service.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.zebra.common.constant.ZebraConstant;

/**
 * 数据初始化
 * @author owen
 */
@Service
public class DataInitService  {

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> valueOps;
    
    /**
     * 获取电池信息，根据主键 batteryCode
     * @return
     */
    @PostConstruct
    public void initData(){
        //1、设置城市是否开通了斑马电车
        //3301:杭州 0516:徐州
        String[] arrCity = {"0571","022","0516"};
        for(String city : arrCity){
            valueOps.set(ZebraConstant.REIDS_CITY_OPEN + city, "open");   
        }
        
    }

}
