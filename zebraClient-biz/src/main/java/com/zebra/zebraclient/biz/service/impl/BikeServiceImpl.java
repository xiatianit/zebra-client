package com.zebra.zebraclient.biz.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.zebra.zebraclient.biz.pojo.viewobject.OrderUnComplatedVO;
import com.zebra.zebraclient.biz.service.PayCenterService;
import com.zebra.zebraclient.dal.dataobject.*;
import com.zebra.zebraclient.dal.mapper.BikeChargePriceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.zebra.common.constant.ZebraConstant;
import com.zebra.common.dto.BikeRelInfoDTO;
import com.zebra.common.dto.BikeRelInfoRdsDTO;
import com.zebra.common.util.GPSTransformMars;
import com.zebra.pony.common.model.Result;
import com.zebra.pony.common.utils.ResultUtils;
import com.zebra.pony.util.DateUtil;
import com.zebra.pony.util.HttpUtil;
import com.zebra.pony.util.JsonUtil;
import com.zebra.pony.util.StringUtil;
import com.zebra.zebraclient.biz.pojo.DynamicParamConfig;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;
import com.zebra.zebraclient.biz.pojo.viewobject.RidingDetailVO;
import com.zebra.zebraclient.biz.pojo.viewobject.RidingEndVO;
import com.zebra.zebraclient.biz.service.BikeService;
import com.zebra.zebraclient.biz.service.UserExtendService;
import com.zebra.zebraclient.biz.service.UserOrderService;
import com.zebra.zebraclient.dal.dataobject.businessObject.BikeBO;
import com.zebra.zebraclient.dal.dataobject.businessObject.SiteNearBO;
import com.zebra.zebraclient.dal.mapper.BikeLocusMapper;
import com.zebra.zebraclient.dal.mapper.BikeMapper;
import com.zebra.zebraclient.dal.mapper.SiteMapper;

/**
 * 电车服务信息
 *
 * @author owen
 */
@Service
public class BikeServiceImpl implements BikeService {

    private final static Logger logger = LoggerFactory.getLogger(BikeServiceImpl.class);

    @Autowired
    private BikeMapper slaveBikeMapper;

    @Autowired
    private BikeMapper masterBikeMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserOrderService userOrderService;

    @Autowired
    private BikeLocusMapper slaveBikeLocusMapper;

    @Autowired
    private SiteMapper slaveSiteMapper;

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> valueOps;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserExtendService userExtendService;

    @Autowired
    private DynamicParamConfig dynamicParamConfig;

    @Autowired
    private BikeChargePriceMapper slaveBikeChargePriceMapper;

    @Autowired
    private PayCenterService payCenterService;

    /**
     * 获取最近的电车信息
     *
     * @param bikeBO
     * @return
     */
    @Override
    public List<BikeDO> queryNearBike(BikeBO bikeBO) {
        logger.info("获取附近电车信息,bikeBO={}", bikeBO);
        List<BikeDO> listBikeDO = slaveBikeMapper.selectNearBikeByCondition(bikeBO);

        // 如果目的地址不为空，则找出用户当前坐标，半径为radius的信息 这找出根据电量来获取数据
        if (listBikeDO == null || listBikeDO.size() == 0) {
            return null;
        }
        return listBikeDO;
    }

    /**
     * 根据bike获取车辆信息
     *
     * @param bikeCode
     * @return
     */
    @Override
    public BikeDO queryBikeRelInfo(String bikeCode) {
        BikeDO bikeDO = slaveBikeMapper.selectByPk(bikeCode);
        if (bikeDO == null) {
            return null;
        }
        return bikeDO;
    }

    /**
     * 根据bike获取车辆信息
     *
     * @param bikeNo
     * @return
     */
    @Override
    public BikeDO queryBikeRelInfoByBikeNo(String bikeNo){
        BikeDO bikeDO = slaveBikeMapper.selectByBikeNo(bikeNo);
        if (bikeDO == null) {
            return null;
        }
        return bikeDO;
    }

    /***
     * 通知终端发出铃声找出车辆
     *
     * @param bikeCode 单车code
     */
    @Override
    public void doRingSearchBike(String bikeCode) {
        logger.info("mq通知终端发出铃声寻车，bikeCode={}", bikeCode);
        BikeDO bikeDO = slaveBikeMapper.selectByPk(bikeCode);
        String jsv = valueOps.get(ZebraConstant.REDIS_BIKE_REL_INFO + bikeCode);
        BikeRelInfoRdsDTO bikeRelInfoRdsDTO = JsonUtil.getObjectFromJson(jsv, BikeRelInfoRdsDTO.class);
        //String t = bikeRelInfoRdsDTO.getTerminalAddress();
        //String[] terminalAddressArr = t.split(":");
        BikeRelInfoDTO bikeRelInfoDTO = new BikeRelInfoDTO(bikeCode, bikeDO.getBatteryCode(), null, null, null,
                null);
        rabbitTemplate.convertAndSend(ZebraConstant.MQ_RING, JsonUtil.getJsonFromObject(bikeRelInfoDTO));
    }

    /***
     * 开锁
     *
     * @param bikeCode 单车code
     */
    @Override
    public Result<?> doUnLockBike(String bikeCode, UserToken userToken) {
        logger.info("确认开锁，bikeCode={},userToken={}", bikeCode, userToken);
        BikeDO bikeDO = slaveBikeMapper.selectByPk(bikeCode);

        // 1、查下此车有么有订单行驶中
        //fixme 后期可以通过该车子状态，有没有在行驶中
        String isBikeOrdering = valueOps.get(ZebraConstant.REIDS_ORDERING + bikeCode);
        logger.info("查下此车有么有订单行驶中，bikeCode={},bikeBattery={},isBikeOrdering={}", bikeCode, bikeDO.getBatteryCode(), isBikeOrdering);
        if (!StringUtil.isBlank(isBikeOrdering)) {
            return ResultUtils.genResultWithSuccess("status", 1, "message", "被人捷足先登了。");
        }

        // 2、通过队列，让终端应用服务netty推送至电车终端
        BikeRelInfoDTO bikeRelInfoDTO = new BikeRelInfoDTO(bikeCode, bikeDO.getBatteryCode(), null, null, userToken.getUid(), null);
        rabbitTemplate.convertAndSend(ZebraConstant.MQ_UNLOCK, JsonUtil.getJsonFromObject(bikeRelInfoDTO));
        return ResultUtils.genResultWithSuccess("status", 0, "message", "可以使用啦");
    }

    /***
     * 结束骑行，并上锁
     *
     * @param bikeCode  单车code
     */
    @SuppressWarnings("unchecked")
    @Override
    public Result<RidingEndVO> doEndDriveAndLockBike(String orderId, String bikeCode, Double totalDistance, UserToken userToken) {

        UserOrderDO userOrderDO = userOrderService.queryUserOrderByPk(orderId);
        // 获取车的即时信息
        String jsv = valueOps.get(ZebraConstant.REDIS_BIKE_REL_INFO + bikeCode);
        BikeRelInfoRdsDTO bikeRelInfoRdsDTO = JsonUtil.getObjectFromJson(jsv, BikeRelInfoRdsDTO.class);

        // 0、判断即将停车与最近的site距离，不能超过50m,超过50m，就不能停
        int userLockNearSiteRadius = dynamicParamConfig.getUserLockNearSiteRadius();
        SiteNearBO siteNearBO = new SiteNearBO();
        siteNearBO.setLocalLongitude(bikeRelInfoRdsDTO.getLongitude());
        siteNearBO.setLocalLatitude(bikeRelInfoRdsDTO.getLatitude());
        siteNearBO.setRadius(userLockNearSiteRadius);

        logger.info("判断即将停车与最近的维修点距离，不能超过{}m,超过{}m，就不能停siteNearBO={}", userLockNearSiteRadius, userLockNearSiteRadius, JsonUtil.getJsonFromObject(siteNearBO));
        List<SiteDO> siteList = slaveSiteMapper.selectNearSiteByPoint(siteNearBO);

        logger.info("判断即将停车与最近的维修点距离，不能超过50m,超过50m，就不能停siteList={}", JsonUtil.getJsonFromObject(siteList));
        if (siteList == null || siteList.size() == 0) {
            RidingEndVO ridingEndVO = new RidingEndVO();
            ridingEndVO.setIsLock(0);
            return ResultUtils.genResultWithSuccess(ridingEndVO);
        }

        // 1、 修改订单订单结束 、未支付。
        // 订单状态：设置骑行结束
        userOrderDO.setOrderStatus(ZebraConstant.USER_ORDER_STATUS_RIDING_END);
        userOrderDO.setEndLongitude(bikeRelInfoRdsDTO.getLongitude());
        userOrderDO.setEndLatitude(bikeRelInfoRdsDTO.getLatitude());
        userOrderDO.setEndTime(DateUtil.getCurrentTimeSeconds());

        // 调用高德地图接口获取具体的地址信息
        double[] lagLng = GPSTransformMars.transLatLng(bikeRelInfoRdsDTO.getLatitude().doubleValue(), bikeRelInfoRdsDTO.getLongitude().doubleValue());
        String url = ZebraConstant.GEOCODE_REGEO + "?key=" + ZebraConstant.GEOCODE_KEY + "&location=" + lagLng[1] + "," + lagLng[0];

        // String url = ZebraConstant.GEOCODE_REGEO + "?key=" + ZebraConstant.GEOCODE_KEY + "&location=" + bikeRelInfoRdsDTO.getLongitude() + ","
        // + bikeRelInfoRdsDTO.getLatitude();
        String resp = HttpUtil.get(url);
        logger.info("请求该点逆地址编码信息，url={},resp={}", url, resp);
        // 时时获取高德地图，该point的详细地址信息
        String s1 = (String) resp.subSequence(resp.lastIndexOf("formatted_address\":\"") + 20, resp.length());
        String geoCodePointAddress = (String) s1.subSequence(0, s1.indexOf("\""));
        userOrderDO.setEndAddress(geoCodePointAddress);
        userOrderDO.setTotalDistance(totalDistance);

        // 骑行总时长
        BikeDO bikeDO = slaveBikeMapper.selectByPk(bikeCode);
        int ridingMins = DateUtil.getDiffMinites(userOrderDO.getStartTime());
        BikeChargePriceDO bikeChargePriceDO = new BikeChargePriceDO();
        bikeChargePriceDO.setBikeModelName(bikeDO.getBikeModel());
        bikeChargePriceDO.setFranchiserCode(bikeDO.getFranchiserCode());
        BikeChargePriceDO tempBikeChargePriceDO = slaveBikeChargePriceMapper.selectBikeChargePrice(bikeChargePriceDO);
        String priceDesc = "10min/2￥";
        if (tempBikeChargePriceDO == null) {
            if (ridingMins <= 10) {
                userOrderDO.setTotalAmount(dynamicParamConfig.getLowestCost());
                userOrderDO.setPayAmount(dynamicParamConfig.getLowestCost());
            } else {
                // 超过10分钟怎么计算金额 1分钟0.1元
                userOrderDO.setTotalAmount(ridingMins * dynamicParamConfig.getPricePerMin());
                userOrderDO.setPayAmount(ridingMins * dynamicParamConfig.getPricePerMin());
            }
        } else {
            int unit = tempBikeChargePriceDO.getUnit();
            double unitPrice = tempBikeChargePriceDO.getUnitPrice();
            int multiple = ridingMins / unit;
            int less = ridingMins % unit;
            if (less != 0) {
                multiple = multiple + 1;
            }
            userOrderDO.setTotalAmount(multiple * unitPrice);
            userOrderDO.setPayAmount(multiple * unitPrice);
            priceDesc = unit + "min/" + unitPrice + "￥";
        }

        userOrderDO.setKmUnitPrice(0.0);
        userOrderDO.setHourUnitPrice(userOrderDO.getTotalAmount());
        userOrderService.editUserOrder(userOrderDO);

        // 2、设置redis 该车、电池正在行驶订单中 删除
        stringRedisTemplate.delete(ZebraConstant.REIDS_ORDERING + bikeCode);

        RidingEndVO ridingEndVO = new RidingEndVO();
        ridingEndVO.setOrderId(orderId);
        ridingEndVO.setPrice(userOrderDO.getTotalAmount());
        ridingEndVO.setPriceDesc(priceDesc);
        ridingEndVO.setTimePrice(userOrderDO.getTotalAmount());
        ridingEndVO.setRidingDistAnce(totalDistance);
        ridingEndVO.setMobiledTime(ridingMins);
        ridingEndVO.setIsLock(1);

        // 3、如果是0元订单 则自动修改支付状态为已支付
        logger.info("订单金额，orderId={},ridingEndVO.getPrice()={}", orderId, ridingEndVO.getPrice());
        if (ridingEndVO.getPrice() == null || ridingEndVO.getPrice() <= 0.01) {
            logger.info("该订单为0元，自动设置为已支付状态；orderId：" + orderId);
            payCenterService.editUserOrder(orderId, ZebraConstant.USER_ORDER_PAY_TYPE_ALIPAY, 0.0,
                    ZebraConstant.USER_ORDER_PAY_STATUS_YES);
        }

        // 4、正常结束订单、并且前端闯传过来得行驶距离不为空，且大于 0,则记录总节约时间：
        if (totalDistance != null && totalDistance.compareTo(0.0d) == 1) {
            logger.info("累计统计用户节约行程");
            UserExtendDO userExtendDO = new UserExtendDO();
            UserExtendDO userExtend = userExtendService.queryUserExtendByUid(userOrderDO.getUid());
            if (userExtend != null) {
                logger.info("存在该用户的骑行记录，则累加该用户的骑行数据：行驶里程，累计用时，总节约时间，总节约支出，uid={}", userOrderDO.getUid());
                userExtendDO.setUid(userOrderDO.getUid());
                Double totalDis = userExtend.getTotalDistance() + totalDistance;
                Double totalAmo = userExtend.getTotalFee() + userOrderDO.getTotalAmount();
                Integer totalRid = userExtend.getTotalRidetime() + ridingMins;
                // 累计骑行总里程
                userExtendDO.setTotalDistance(userExtend.getTotalDistance() + totalDistance);
                // 总节约用时(单位分钟)：60(分钟/小时) * ［累计行驶里程（公里） / 自行车匀速10（公里/小时）］ - 累计用时（分钟）
                userExtendDO.setSaveTime(60 * (totalDis.intValue() / 10) - (totalRid));
                // 总节约支出(单位公里)：2（元/公里） * 累计行驶里程（公里） - 累计费用（元）
                userExtendDO.setSavePrice(2 * (totalDis) - (totalAmo));
                // 累计骑行总费用
                userExtendDO.setTotalFee(totalAmo);
                // 累计骑行总用时
                userExtendDO.setTotalRidetime(totalRid);
                userExtendService.editUserExtend(userExtendDO);
            } else {
                logger.info("该用户是第一次骑行,插入该用户骑行的数据，uid={},totalDistance={},saveTime={},savePrice={},totalFee={},totalRidetime={}",
                        userOrderDO.getUid(), totalDistance, 60 * (totalDistance.intValue() / 10) - ridingMins,
                        2 * totalDistance - userOrderDO.getTotalAmount(), userOrderDO.getTotalAmount(), ridingMins);
                userExtendDO.setUid(userOrderDO.getUid());
                userExtendDO.setTotalDistance(totalDistance);
                userExtendDO.setSaveTime(60 * (totalDistance.intValue() / 10) - ridingMins);
                userExtendDO.setSavePrice(2 * totalDistance - userOrderDO.getTotalAmount());
                userExtendDO.setTotalFee(userOrderDO.getTotalAmount());
                userExtendDO.setTotalRidetime(ridingMins);
                userExtendService.addUserExtend(userExtendDO);
            }
        }

        //5、更改单车点状态：修改为未行驶状态。 fixme
        logger.info("doEndDriveAndLockBike.结束骑行,单车状态前[" + bikeDO.getBikeStatus() + "],单车上锁状态前[" + bikeDO.getLockStatus() + "],-->单车状态后[" + ZebraConstant.BIKE_STATUS_3 + "],单车上锁状态后[4]");
        bikeDO.setBikeStatus(ZebraConstant.BIKE_STATUS_3);
        bikeDO.setLockStatus(4);

        //String url = ZebraConstant.GEOCODE_REGEO + "?key=" + ZebraConstant.GEOCODE_KEY + "&location=" + termiToServReq1.getLongitude().doubleValue() + "," + termiToServReq1.getLatitude().doubleValue();
        //String resp = HttpUtil.get(url);
        //String s1 = (String) resp.subSequence(resp.lastIndexOf("formatted_address\":\"") + 20, resp.length());
        //String geoCodePointAddress = (String) s1.subSequence(0, s1.indexOf("\""));
        bikeDO.setCurrentAddress(geoCodePointAddress);
        // 设置redis 该车、电池正在行驶订单中 删除，再次删除，如果是不在当前所在车的服务区。\，
        stringRedisTemplate.delete(ZebraConstant.REIDS_ORDERING + bikeCode);
        masterBikeMapper.update(bikeDO);

        // 6、通过队列，让终端应用服务netty推送至电车终端
        logger.info("结束订单，向netty服务器发送mq信息通知，让netty服务器向电车终端车发送信息");
        //String t = bikeRelInfoRdsDTO.getTerminalAddress();
        //String[] terminalAddressArr = t.split(":");
        BikeRelInfoDTO bikeRelInfoDTO = new BikeRelInfoDTO(bikeCode, userOrderDO.getBatteryCode(), null, null,
                userOrderDO.getUid(), orderId);
        logger.info("发送mq.结束订单");
        rabbitTemplate.convertAndSend(ZebraConstant.MQ_LOCK, JsonUtil.getJsonFromObject(bikeRelInfoDTO));
        ridingEndVO.setBikeNo(bikeDO.getBikeNo());
        return ResultUtils.genResultWithSuccess(ridingEndVO);
    }

    /**
     * 获取当前行驶数据
     *
     * @param orderId
     * @param bikeCode
     * @param userToken
     */
    @SuppressWarnings("unchecked")
    @Override
    public Result<RidingDetailVO> getCurrentRideDetail(String orderId, String bikeCode, UserToken userToken) {
        logger.info("获取当前行驶数据，orderId={},bikeCode={},userToken={}", orderId, bikeCode, userToken);
        UserOrderDO userOrderDO = userOrderService.queryUserOrderByPk(orderId);
        String jsv = valueOps.get(ZebraConstant.REDIS_BIKE_REL_INFO + bikeCode);
        BikeRelInfoRdsDTO bikeRelInfoRdsDTO = JsonUtil.getObjectFromJson(jsv, BikeRelInfoRdsDTO.class);
        RidingDetailVO ridingDetailVO = new RidingDetailVO();
        ridingDetailVO.setCurrentLatitude(bikeRelInfoRdsDTO.getLatitude());
        ridingDetailVO.setCurrentLongitude(bikeRelInfoRdsDTO.getLongitude());

        ridingDetailVO.setLongitude(userOrderDO.getStartLongitude());
        ridingDetailVO.setLatitude(userOrderDO.getStartLatitude());
        ridingDetailVO.setElecQuanlity(bikeRelInfoRdsDTO.getElecQuanlity());
        ridingDetailVO.setLeftMiles(bikeRelInfoRdsDTO.getElecQuanlity() * dynamicParamConfig.getBatteryFullMils() / 100);
        ridingDetailVO.setOrderId(orderId);
        int ridingMins = DateUtil.getDiffMinites(userOrderDO.getStartTime());

        String isOrderingValue = valueOps.get(ZebraConstant.REIDS_ORDERING + bikeCode);
        logger.info("获取当前行驶数据orderId={},isOrderingValue={},isOrderingValue为空，则订单结束", orderId, isOrderingValue);
        if (StringUtil.isBlank(isOrderingValue)) {
            // 订单又没结束骑行，如果车子超出行驶范围外，则会结束订单。 此时订单已经结束，但在这里需提示用户
            ridingDetailVO.setOrderStatus(ZebraConstant.USER_ORDER_STATUS_RIDING_END);
            ridingDetailVO.setDistancePrice(0.0);
            ridingDetailVO.setRidingDistAnce(0.0);
            ridingDetailVO.setIsOutFench(1);
            if (userOrderDO.getEndTime() == null) {
                ridingMins = DateUtil.getDiffMinites(userOrderDO.getStartTime());
            } else {
                ridingMins = DateUtil.getDiffMinites(userOrderDO.getStartTime(), userOrderDO.getEndTime());
            }
        } else {
            ridingDetailVO.setOrderStatus(ZebraConstant.USER_ORDER_STATUS_RIDING_COMMON);
            ridingDetailVO.setIsOutFench(0);
        }

        // 计算价格时，到时需统一计算
        logger.info("已行驶ridingMins={}", ridingMins);
        ridingDetailVO.setMinite(ridingMins);
        ridingDetailVO.setMobiledTime(ridingMins + "分钟");
        BikeDO bikeDO = slaveBikeMapper.selectByPk(bikeCode);
        BikeChargePriceDO bikeChargePriceDO = new BikeChargePriceDO();
        bikeChargePriceDO.setBikeModelName(bikeDO.getBikeModel());
        bikeChargePriceDO.setFranchiserCode(bikeDO.getFranchiserCode());
        BikeChargePriceDO tempBikeChargePriceDO = slaveBikeChargePriceMapper.selectBikeChargePrice(bikeChargePriceDO);
        if (tempBikeChargePriceDO == null) {
            if (ridingMins <= 10) {
                userOrderDO.setTotalAmount(dynamicParamConfig.getLowestCost());
                userOrderDO.setPayAmount(dynamicParamConfig.getLowestCost());
            } else {
                // 超过10分钟怎么计算金额 1分钟0.1元
                userOrderDO.setTotalAmount(ridingMins * dynamicParamConfig.getPricePerMin());
                userOrderDO.setPayAmount(ridingMins * dynamicParamConfig.getPricePerMin());
            }
        } else {
            int unit = tempBikeChargePriceDO.getUnit();
            double unitPrice = tempBikeChargePriceDO.getUnitPrice();
            int multiple = ridingMins / unit;
            int less = ridingMins % unit;
            if (less != 0) {
                multiple = multiple + 1;
            }
            userOrderDO.setTotalAmount(multiple * unitPrice);
            userOrderDO.setPayAmount(multiple * unitPrice);
            ridingDetailVO.setPriceDesc(unit + "min/" + unitPrice + "元");
        }

        logger.info("ridingDetailVO.bike={},bikeNo={}", JsonUtil.getJsonFromObject(bikeDO), bikeDO.getBikeNo());
        ridingDetailVO.setBikeNo(bikeDO.getBikeNo());
        ridingDetailVO.setPrice(userOrderDO.getTotalAmount());

        //设置车子模型
        ridingDetailVO.setBikeModel(bikeDO.getBikeModel());

        // 获取该订单行驶轨迹
        BikeLocusDO bikeLocusDO = new BikeLocusDO();
        bikeLocusDO.setBikeCode(bikeCode);
        bikeLocusDO.setOrderId(orderId);
        ridingDetailVO.setRouteList(slaveBikeLocusMapper.selectByCondition(bikeLocusDO));

        //fixme  价格描述
        logger.info("ridingDetailVO={}", JsonUtil.getJsonFromObject(ridingDetailVO));

        return ResultUtils.genResultWithSuccess(ridingDetailVO);
    }

    /**
     * 临时停车
     *
     * @param bikeCode
     * @param userToken
     * @param orderId
     * @return
     */
    @Override
    public Result<OrderUnComplatedVO> pauseRide(String bikeCode, UserToken userToken, String orderId) {
        logger.info("临时停车，bikeCode={},userToken={}，orderId={}", bikeCode, userToken, orderId);
        UserOrderDO userOrderDO = userOrderService.queryUserOrderByPk(orderId);
        // 获取车的即时信息
        String jsv = valueOps.get(ZebraConstant.REDIS_BIKE_REL_INFO + bikeCode);
        BikeRelInfoRdsDTO bikeRelInfoRdsDTO = JsonUtil.getObjectFromJson(jsv, BikeRelInfoRdsDTO.class);
        //1. 获取订单最新数据 生成返回数据对象
        OrderUnComplatedVO orderUnComplatedVO = new OrderUnComplatedVO(3, userOrderDO.getOrderId(), userOrderDO.getBikeCode());
        int ridingMins = DateUtil.getDiffMinites(userOrderDO.getStartTime());

        orderUnComplatedVO.setPrice(userOrderDO.getTotalAmount());
        //orderUnComplatedVO.setPriceDesc(dynamicParamConfig.getPriceDesc());

        BikeDO bikeDO = slaveBikeMapper.selectByPk(bikeCode);
        BikeChargePriceDO bikeChargePriceDO = new BikeChargePriceDO();
        bikeChargePriceDO.setBikeModelName(bikeDO.getBikeModel());
        bikeChargePriceDO.setFranchiserCode(bikeDO.getFranchiserCode());
        BikeChargePriceDO tempBikeChargePriceDO = slaveBikeChargePriceMapper.selectBikeChargePrice(bikeChargePriceDO);
        if (tempBikeChargePriceDO != null) {
            int unit = tempBikeChargePriceDO.getUnit();
            double unitPrice = tempBikeChargePriceDO.getUnitPrice();
            int multiple = ridingMins / unit;
            int less = ridingMins % unit;
            if (less != 0) {
                multiple = multiple + 1;
            }
            orderUnComplatedVO.setPrice(multiple * unitPrice);
        }
        //orderUnComplatedVO.setTimePrice(userOrderDO.getTotalAmount());

        orderUnComplatedVO.setRidingDistAnce(userOrderDO.getTotalDistance());
        orderUnComplatedVO.setBikeNo(bikeDO.getBikeNo());
        orderUnComplatedVO.setMobiledTime(ridingMins);
        logger.info("订单最新数据：OrderUnComplatedVO={}" + orderUnComplatedVO);

        // 2. 通知终端上锁  通过队列，让终端应用服务netty推送至电车终端
        // 终端负责锁车并且修改车状态为临时停车状态
        logger.info("临时停车，向netty服务器发送mq信息通知，让netty服务器向电车终端车发送信息");
        //String t = bikeRelInfoRdsDTO.getTerminalAddress();
        //String[] terminalAddressArr = t.split(":");
        BikeRelInfoDTO bikeRelInfoDTO = new BikeRelInfoDTO(bikeCode, userOrderDO.getBatteryCode(), null, null,
                userOrderDO.getUid(), orderId);
        logger.info("发送mq.关锁");
        rabbitTemplate.convertAndSend(ZebraConstant.MQ_PAUSERIDE_LOCK, JsonUtil.getJsonFromObject(bikeRelInfoDTO));
        return ResultUtils.genResultWithSuccess(orderUnComplatedVO);

    }


    /**
     * 继续骑行
     *
     * @param bikeNo
     * @param userToken
     * @param orderId
     * @return
     */
    @Override
    public Result<?> continueRide(String bikeNo, UserToken userToken, String orderId) {
        logger.info("继续骑行，bikeNo={},userToken={}，orderId={}", bikeNo, userToken, orderId);
        //BikeDO bikeDO = slaveBikeMapper.selectByPk(bikeCode);
        BikeDO bikeDO = slaveBikeMapper.selectByBikeNo(bikeNo);

        // 1、查下此车状态 是否处于行驶中并且是否处于临时停车状态
        //fixme 后期可以通过该车子状态，有没有在行驶中
        String bikeCode = bikeDO.getBikeCode();
        String isBikeOrdering = valueOps.get(ZebraConstant.REIDS_ORDERING + bikeCode);
        logger.info("查下此车有么有订单行驶中，bikeCode={},bikeNo={},bikeBattery={},isBikeOrdering={}", bikeCode,bikeNo, bikeDO.getBatteryCode(), isBikeOrdering);
        if (StringUtil.isBlank(isBikeOrdering) || bikeDO.getBikeStatus() != ZebraConstant.BIKE_STATUS_12) {
            return ResultUtils.genResultWithSuccess("status", 1, "message", "该车不是你要找的那辆哦~");
        }
        // 2、查询该车的订单信息是否同当前用户吻合
        UserOrderDO userOrderDO = userOrderService.queryUserOrderByPk(orderId);
        if (userToken.getUid().intValue() != userOrderDO.getUid().intValue()) {
            return ResultUtils.genResultWithSuccess("status", 1, "message", "该车不是你要找的那辆哦~");

        }

        // 3、开锁 通过队列，让终端应用服务netty推送至电车终端
        BikeRelInfoDTO bikeRelInfoDTO = new BikeRelInfoDTO(bikeCode, bikeDO.getBatteryCode(), null, null, userToken.getUid(), null);
        rabbitTemplate.convertAndSend(ZebraConstant.MQ_CONTINUERIDE_UNLOCK, JsonUtil.getJsonFromObject(bikeRelInfoDTO));
        return ResultUtils.genResultWithSuccess("status", 0, "message", "可以继续骑行了");

    }


    public static void main(String[] args) {
        int s = (int) (System.currentTimeMillis() / 1000L);
        try {
            Thread.sleep(10000L);
        } catch (Exception e) {

        }

        int e = (int) (System.currentTimeMillis() / 1000L);
        int min = DateUtil.getDiffMinites(s, e);
        System.out.println("min=" + min);

    }


}
