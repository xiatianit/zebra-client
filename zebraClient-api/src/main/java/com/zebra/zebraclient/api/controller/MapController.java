package com.zebra.zebraclient.api.controller;

import com.zebra.common.constant.ZebraConstant;
import com.zebra.pony.common.model.Result;
import com.zebra.pony.common.utils.ResultUtils;
import com.zebra.pony.util.JsonUtil;
import com.zebra.pony.util.StringUtil;
import com.zebra.zebraclient.api.helper.TokenHelper;
import com.zebra.zebraclient.biz.enums.ResultErrorEnum;
import com.zebra.zebraclient.biz.pojo.DynamicParamConfig;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.pojo.converter.BikeConverter;
import com.zebra.zebraclient.biz.pojo.paramobject.QueryNearBikeReqPO;
import com.zebra.zebraclient.biz.pojo.viewobject.BikeRelInfoVO;
import com.zebra.zebraclient.biz.pojo.viewobject.QueryNearBikeRespVO;
import com.zebra.zebraclient.biz.pojo.viewobject.RidingDetailVO;
import com.zebra.zebraclient.biz.pojo.viewobject.RidingEndVO;
import com.zebra.zebraclient.biz.service.BikeService;
import com.zebra.zebraclient.biz.service.PrefectureService;
import com.zebra.zebraclient.biz.service.UserOrderService;
import com.zebra.zebraclient.biz.service.UserService;
import com.zebra.zebraclient.dal.dataobject.BikeDO;
import com.zebra.zebraclient.dal.dataobject.CityDO;
import com.zebra.zebraclient.dal.dataobject.UserBaseDO;
import com.zebra.zebraclient.dal.dataobject.UserOrderDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 与手机地图相关接口controlellr
 *
 * @author owen
 */
@RestController
@RequestMapping("/mapapi")
public class MapController {

    private final static Logger logger = LoggerFactory.getLogger(MapController.class);

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private BikeService bikeService;

    @Autowired
    private PrefectureService prefectureService;

    @Autowired
    private UserOrderService userOrderService;

    @Autowired
    private DynamicParamConfig dynamicParamConfig;

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> valueOps;

    /**
     * 获取用户附近的车辆信息 PS：获取用户所在地至目的的车辆信息
     *
     * @param token
     * @return
     */
    @RequestMapping(value = "/nearbyBikeList", method = RequestMethod.GET)
    public Result<?> getNearbyBikeList(@RequestParam(value = "token", required = true) String token,
                                       @RequestParam(value = "localLongitude", required = true) BigDecimal localLongitude,
                                       @RequestParam(value = "localLatitude", required = true) BigDecimal localLatitude,
                                       @RequestParam(value = "radius", required = true) Integer radius,
                                       @RequestParam(value = "cityCode", required = true) String cityCode,
                                       @RequestParam(value = "zoneCode", required = true) Integer zoneCode) {
        QueryNearBikeReqPO queryNearBikeReqPO = new QueryNearBikeReqPO(localLongitude, localLatitude, radius, cityCode, zoneCode);
        logger.info("获取用户周边车辆信息：token={},queryNearBikeReqPO={}", token, JsonUtil.getJsonFromObject(queryNearBikeReqPO));
        try {
            queryNearBikeReqPO.setRadius(dynamicParamConfig.getUserSearchNearBikeRadius());
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.getNearbyBikeListo:token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            // "type": 0表示无错误,1表示当前城市尚未开通,2表示周围无车
//            String cityIsOpenValue = valueOps.get(ZebraConstant.REIDS_CITY_OPEN + cityCode);
//            if (StringUtil.isBlank(cityIsOpenValue)) {
//                logger.info("该城市未开通运营，cityCode={}", cityCode);
//                return ResultUtils.genResultWithSuccess("type", 1, "list", null);
//            }

            List<BikeDO> listBikeDO = bikeService.queryNearBike(BikeConverter.nearBikeReqPO2BikeBO(queryNearBikeReqPO));
            if (listBikeDO == null || listBikeDO.size() == 0) {
                logger.info("并未有车辆，cityCode={}", cityCode);
                return ResultUtils.genResultWithSuccess("type", 2, "list", null);
            }

            List<QueryNearBikeRespVO> listVO = BikeConverter.bikeDO2NearBikeRespVO(listBikeDO, queryNearBikeReqPO, dynamicParamConfig.getPriceDesc());
            logger.info("queryNearBikeReqPO={},listVO={}", queryNearBikeReqPO, listVO);
            return ResultUtils.genResultWithSuccess("type", 0, "list", listVO);
        } catch (Exception e) {
            logger.error("获取用户周边车辆信息失败,token={}，queryNearBikeReqPO={},e={}", token, JsonUtil.getJsonFromObject(queryNearBikeReqPO), e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 经纬度用以判断车辆是否在可以被"吁一下"的范围内 对车辆发出吁一下的命令
     *
     * @param token
     * @param longitude
     * @param latitude
     * @param bikeCode
     * @return
     */
    @RequestMapping(value = "/ringCallBike", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doRingCallBike(@RequestParam(value = "token", required = true) String token,
                                    @RequestParam(value = "longitude", required = true) BigDecimal longitude,
                                    @RequestParam(value = "latitude", required = true) BigDecimal latitude, @RequestParam(value = "bikeCode", required = true) String bikeCode) {
        logger.info("用户吁一下车辆：token={},longitude={},latitude={},bikeCode={}", token, longitude, latitude, bikeCode);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.doRingCallBike:token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            bikeService.doRingSearchBike(bikeCode);
            return ResultUtils.genResultWithSuccess(null);
        } catch (Exception e) {
            logger.error("用户吁一下车辆信息失败：token={},longitude={},latitude={},bikeCode={},e={}", token, longitude, latitude, bikeCode, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }



    /**
     * 扫码开锁,获取车辆信息
     *
     * @param token    token
     * @param bikeCode 单车code
     * @return Result<?>
     */
    @RequestMapping(value = "/openLock", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> doGetOpenlockBikeInfo(@RequestParam(value = "token", required = true) String token,
                                           @RequestParam(value = "bikeCode", required = true) String bikeCode) {
        logger.info("用户扫码，获取车辆信息：token={},bikeNo=bikeCode={}", token, bikeCode);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.doGetOpenlockBikeInfo,token={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            BikeDO bikeDO = bikeService.queryBikeRelInfoByBikeNo(bikeCode);
            // BikeDO bikeDO = bikeService.queryBikeRelInfo(bikeCode);
            logger.info("MapController.doGetOpenlockBikeInfo,bikeDO={}", JsonUtil.getJsonFromObject(userToken), JsonUtil.getJsonFromObject(bikeDO));
            BikeRelInfoVO bikeRelInfoVO = null;
            if (bikeDO.getBikeStatus().intValue() == ZebraConstant.BIKE_STATUS_3) {
                // 该车可行驶
                bikeRelInfoVO = BikeConverter.bikeDO2BikeRelInfoVO(bikeDO, 0, dynamicParamConfig.getPriceDesc());
            } else {
                // 该车不可行驶
                bikeRelInfoVO = BikeConverter.bikeDO2BikeRelInfoVO(bikeDO, 1, dynamicParamConfig.getPriceDesc());
            }
            return ResultUtils.genResultWithSuccess(bikeRelInfoVO);
        } catch (Exception e) {
            logger.error("用户扫码，获取车辆信息异常：token={},,bikeCode={},e={}", token, bikeCode, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 用户确认开锁，生成订单号，并返回订单号 orderId
     *
     * @param token    token
     * @param bikeCode 单车code
     * @return Result<?>
     */
    @RequestMapping(value = "/confirmOpenLock", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> doConfirmopenlock(@RequestParam(value = "token", required = true) String token,
                                       @RequestParam(value = "bikeCode", required = true) String bikeCode) {
        logger.info("用户确认开锁：token={},bikeCode={}", token, bikeCode);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.doGetOpenlockBikeInfo={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            return bikeService.doUnLockBike(bikeCode, userToken);
        } catch (Exception e) {
            logger.error("用户确认开锁：token={},bikeCode={},e={}", token, bikeCode, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 行驶中，获取订单信息 获取当前行驶数据
     *
     * @param token
     * @param bikeCode
     * @param orderId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getRidingDetail", method = RequestMethod.GET)
    @ResponseBody
    public Result<RidingDetailVO> getRidingDetail(@RequestParam(value = "token", required = true) String token,
                                                  @RequestParam(value = "bikeCode", required = true) String bikeCode, @RequestParam(value = "orderId", required = false) String orderId) {
        logger.info("行驶中，获取订单信息：token={},bikeCode={},orderId{}", token, bikeCode, orderId);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.doGetOpenlockBikeInfo={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            return bikeService.getCurrentRideDetail(orderId, bikeCode, userToken);
        } catch (Exception e) {
            logger.error("行驶中，获取订单信息异常：token={},bikeCode={},orderId{},e={}", token, bikeCode, orderId, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 结束骑行
     *
     * @param token
     * @param bikeCode
     * @param orderId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/endRide", method = RequestMethod.POST)
    @ResponseBody
    public Result<RidingEndVO> doEndRide(@RequestParam(value = "token", required = true) String token,
                                         @RequestParam(value = "bikeCode", required = true) String bikeCode, @RequestParam(value = "orderId", required = true) String orderId,
                                         @RequestParam(value = "ridingDistAnce", required = true) Double ridingDistAnce) {
        logger.info("结束骑行：token={},bikeCode={},orderId{},ridingDistAnce={}", token, bikeCode, orderId, ridingDistAnce);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.endRide={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            return bikeService.doEndDriveAndLockBike(orderId, bikeCode, ridingDistAnce, userToken);
        } catch (Exception e) {
            logger.error("结束骑行异常：token={},,bikeCode={},orderId{},ridingDistAnce={},e={}", token, bikeCode, orderId, ridingDistAnce, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 获取城市列表
     *
     * @param token
     * @return
     */
    @RequestMapping(value = "/fetchCityList", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> doFetchCityList(@RequestParam(value = "token", required = true) String token) {
        logger.info("获取城市列表：token={}", token);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.doFetchCityList={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }
            List<CityDO> listCityDO = prefectureService.queryCicyByCondition();
            return ResultUtils.genResultWithSuccess(listCityDO);
        } catch (Exception e) {
            logger.error("获取城市列表：token={},e={}", token, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 该车，该用户，是否在行驶中，是否开锁成功。
     *
     * @param token
     * @param bikeCode 0表示已开锁,1表示未开锁
     * @return
     */
    @RequestMapping(value = "/isUnLock", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> doIsUnLock(@RequestParam(value = "token", required = true) String token,
                                @RequestParam(value = "bikeCode", required = true) String bikeCode) {
        logger.info("结束骑行：token={},bikeCode={}", token, bikeCode);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.doIsUnLock={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            UserOrderDO userOrderDO = userOrderService.jugeBikeCurrentRidingStatus(userToken.getUid(), bikeCode);
            logger.info("isUnLock.userOrderDO={}", userOrderDO);
            if (userOrderDO != null) {
                return ResultUtils.genResultWithSuccess("status", 0, "orderId", userOrderDO.getOrderId());
            } else {
                return ResultUtils.genResultWithSuccess("status", 1, "orderId", null);
            }
        } catch (Exception e) {
            logger.error("结束骑行异常：token={},bikeCode={},e={}", token, bikeCode, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }

    /**
     * 临时停车
     *
     * @param token    token
     * @param orderId  订单号
     * @param bikeCode 电车code
     * @return
     */
    @RequestMapping(value = "/pauseride", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> pauseRide(@RequestParam(value = "token", required = true) String token,
                               @RequestParam(value = "orderId", required = true) String orderId,
                               @RequestParam(value = "bikeCode", required = true) String bikeCode) {
        logger.info("临时停车：orderId={}，bikeCode={}", orderId, bikeCode);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.pauseRide={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            // todo fixme
            // 1. 主要上锁
            // 2. 改车状态
            return bikeService.pauseRide(bikeCode, userToken, orderId);

        } catch (Exception e) {
            logger.error("临时停车异常：token={},orderId={},bikeCode={},e={}", token, orderId, bikeCode, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());

        }
    }

    /**
     * 临时停车后  继续骑行
     *
     * @param token
     * @param orderId
     * @param bikeCode
     * @return
     */
    @RequestMapping(value = "/continueride", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> continueRide(
            @RequestParam(value = "token", required = true) String token,
            @RequestParam(value = "orderId", required = true) String orderId,
            @RequestParam(value = "bikeCode", required = true) String bikeCode) {
        logger.info("继续骑行：token={},orderId={}，bikeCode=bikeNo={}", token, orderId, bikeCode);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.pauseRide={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            // todo fixme
            // 1. 开锁
            // 2. 改车状态
            return bikeService.continueRide(bikeCode, userToken, orderId);

        } catch (Exception e) {
            logger.error("继续骑行异常：token={},orderId={},bikeCode={},e={}", token, orderId, bikeCode, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());

        }
    }

    /**
     * 行驶中，获取订单信息 获取当前行驶数据
     *
     * @param token
     * @param bikeCode
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getBikeInfo", method = RequestMethod.GET)
    @ResponseBody
    public Result<BikeDO> getBikeInfo(@RequestParam(value = "token", required = true) String token,
                                      @RequestParam(value = "bikeCode", required = true) String bikeCode) {
        logger.info("获取单车信息：token={},bikeCode={}", token, bikeCode);
        try {
            UserToken userToken = tokenHelper.getUidByToken(token);
            logger.info("MapController.getBikeInfo={},userToken={}", token, JsonUtil.getJsonFromObject(userToken));
            if (userToken == null) {
                return ResultUtils.genResult(ResultErrorEnum.USER_TOKEN_INVALID.getErrorCode(), "token为空");
            }

            BikeDO bikeDO = bikeService.queryBikeRelInfo(bikeCode);
            logger.info("MapController.getBikeInfo,bikeDO={}", JsonUtil.getJsonFromObject(userToken), JsonUtil.getJsonFromObject(bikeDO));
            return ResultUtils.genResultWithSuccess(bikeDO);
        } catch (Exception e) {
            logger.error("获取单车信息：token={},bikeCode={},e={}", token, bikeCode, e);
            return ResultUtils.genResult(ResultUtils.RESULT_STATUS_FAIL, e.getMessage());
        }
    }
}
