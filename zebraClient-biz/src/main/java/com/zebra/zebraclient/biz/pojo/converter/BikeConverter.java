package com.zebra.zebraclient.biz.pojo.converter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.zebra.zebraclient.biz.pojo.paramobject.QueryNearBikeReqPO;
import com.zebra.zebraclient.biz.pojo.viewobject.BikeRelInfoVO;
import com.zebra.zebraclient.biz.pojo.viewobject.QueryNearBikeRespVO;
import com.zebra.zebraclient.dal.dataobject.BikeDO;
import com.zebra.zebraclient.dal.dataobject.businessObject.BikeBO;

/**
 * 地图请求converter
 * 
 * @author owen
 *
 */
public class BikeConverter {

    /***
     * 查询附近车辆，请求对象converter
     * 
     * @param queryNearBikeReqPO
     * @return
     */
    public static BikeBO nearBikeReqPO2BikeBO(QueryNearBikeReqPO queryNearBikeReqPO) {
        if (queryNearBikeReqPO == null) {
            return null;
        }
        BikeBO bikeBO = new BikeBO();
        BeanUtils.copyProperties(queryNearBikeReqPO, bikeBO);
        return bikeBO;
    }

    /**
     * 查询附近车辆，返回对象converter
     * 
     * @param bikeDO
     * @param queryNearBikeReqPO
     * @return
     */
    public static QueryNearBikeRespVO bikeDO2NearBikeRespVO(BikeDO bikeDO, QueryNearBikeReqPO queryNearBikeReqPO, String priceDesc) {
        if (bikeDO == null) {
            return null;
        }

        QueryNearBikeRespVO queryNearBikeRespVO = new QueryNearBikeRespVO();
        BeanUtils.copyProperties(bikeDO, queryNearBikeRespVO);
        BeanUtils.copyProperties(queryNearBikeReqPO, queryNearBikeRespVO);
        queryNearBikeRespVO.setBikeNo(bikeDO.getBikeNo());
        queryNearBikeRespVO.setLocalLongitude(bikeDO.getLongitude());
        queryNearBikeRespVO.setLocalLatitude(bikeDO.getLatitude());

        queryNearBikeRespVO.setPriceDesc(priceDesc);
        queryNearBikeRespVO.setLocation(bikeDO.getCurrentAddress());
        queryNearBikeRespVO.setLeftElec(bikeDO.getBatteryElec());
        queryNearBikeRespVO.setLeftMiles(bikeDO.getRestMileage());
        return queryNearBikeRespVO;
    }

    public static void main(String[] args) {

        Double a = 2772.0;
        System.out.println(a / 1000);
    }

    public static List<QueryNearBikeRespVO> bikeDO2NearBikeRespVO(List<BikeDO> bikeDOs, QueryNearBikeReqPO queryNearBikeReqPO, String priceDesc) {
        if (bikeDOs == null) {
            return null;
        }

        List<QueryNearBikeRespVO> listVO = new ArrayList<QueryNearBikeRespVO>(bikeDOs.size());
        QueryNearBikeRespVO queryNearBikeRespVO = null;
        for (BikeDO bikeDO : bikeDOs) {
            queryNearBikeRespVO = BikeConverter.bikeDO2NearBikeRespVO(bikeDO, queryNearBikeReqPO, priceDesc);
            listVO.add(queryNearBikeRespVO);
        }
        return listVO;
    }

    public static BikeRelInfoVO bikeDO2BikeRelInfoVO(BikeDO bikeDO, int status, String priceDesc) {
        if (bikeDO == null) {
            return null;
        }
        BikeRelInfoVO bikeRelInfoVO = new BikeRelInfoVO();
        BeanUtils.copyProperties(bikeDO, bikeRelInfoVO);
        bikeRelInfoVO.setLeftElec(bikeDO.getBatteryElec());
        bikeRelInfoVO.setLeftMiles(bikeDO.getRestMileage());
        bikeRelInfoVO.setPriceDesc(priceDesc);
        bikeRelInfoVO.setStatus(status);
        bikeRelInfoVO.setBikeNo(bikeDO.getBikeNo());
        return bikeRelInfoVO;
    }

}
