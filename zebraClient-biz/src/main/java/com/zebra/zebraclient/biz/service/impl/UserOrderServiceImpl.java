package com.zebra.zebraclient.biz.service.impl;

import java.text.DecimalFormat;
import java.util.List;

import com.zebra.zebraclient.dal.dataobject.*;
import com.zebra.zebraclient.dal.mapper.BikeChargePriceMapper;
import com.zebra.zebraclient.dal.mapper.BikeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zebra.common.constant.ZebraConstant;
import com.zebra.pony.util.DateUtil;
import com.zebra.zebraclient.biz.pojo.DynamicParamConfig;
import com.zebra.zebraclient.biz.pojo.converter.UserOrderConverter;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderDetailVO;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderUnComplatedVO;
import com.zebra.zebraclient.biz.service.UserOrderService;
import com.zebra.zebraclient.biz.service.UserService;
import com.zebra.zebraclient.dal.mapper.BikeLocusMapper;
import com.zebra.zebraclient.dal.mapper.UserOrderMapper;

/**
 * 电池服务信息
 *
 * @author owen
 */
@Service
public class UserOrderServiceImpl implements UserOrderService {

    private final static Logger logger = LoggerFactory.getLogger(UserOrderServiceImpl.class);

    @Autowired
    private UserOrderMapper slaveUserOrderMapper;

    @Autowired
    private UserOrderMapper masterUserOrderMapper;

    @Autowired
    private BikeLocusMapper slaveBikeLocusMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private DynamicParamConfig dynamicParamConfig;

    @Autowired
    private BikeMapper slaveBikeMapper;
    @Autowired
    private BikeChargePriceMapper slaveBikeChargePriceMapper;


    /**
     * 查询单个订单
     */
    @Override
    public UserOrderDO queryUserOrderByPk(String orderId) {
        return slaveUserOrderMapper.selectByPk(orderId);
    }

    /**
     * 修改订单
     */
    @Override
    public void editUserOrder(UserOrderDO userOrderDO) {
        masterUserOrderMapper.update(userOrderDO);
    }

    /**
     * 查询用户orederID
     *
     * @param uid
     * @return
     */
    @Override
    public List<UserOrderDO> queryUserOrderByUid(Long uid) {
        return slaveUserOrderMapper.selectByUid(uid);
    }

    /**
     * 获取订单信息详情
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderDetailVO queryUserOrderDetailByOrderId(String orderId) {
        logger.info("获取订单详情，orderId={}", orderId);
        UserOrderDO userOrderDO = slaveUserOrderMapper.selectByPk(orderId);
        UserBaseDO userBaseDO = userService.queryByPk(userOrderDO.getUid());

        BikeLocusDO bikeLocusDO = new BikeLocusDO();
        bikeLocusDO.setBikeCode(userOrderDO.getBikeCode());
        bikeLocusDO.setOrderId(orderId);

        OrderDetailVO orderDetailVO = UserOrderConverter.userOrderDO2OrderDetailVO(userOrderDO);
        int ridingMins = DateUtil.getDiffMinites(userOrderDO.getStartTime(), userOrderDO.getEndTime());
        orderDetailVO.setTotalTime(ridingMins + "分钟");
        DecimalFormat df = new DecimalFormat("######0.0");
        orderDetailVO.setSpeed(df.format(userOrderDO.getTotalDistance() * 60 / ridingMins) + "千米/小时");
        orderDetailVO.setPortrait(userBaseDO.getHeadPortrait());
        orderDetailVO.setRouteList(slaveBikeLocusMapper.selectByCondition(bikeLocusDO));
        return orderDetailVO;
    }

    /**
     * 查询用户orederID 用户未完成订单的情况
     *
     * @param uid
     * @return
     */
    @Override
    public OrderUnComplatedVO doJudgeUsersOrderSituation(Long uid) {
        logger.info("查找未完成的订单情况uid={}", uid);
        List<UserOrderDO> list = slaveUserOrderMapper.selectUncomplatedByUid(uid);
        logger.info("查找未完成的订单情况uid={},list={}", uid, list);
        if (list == null || list.size() == 0) {
            return new OrderUnComplatedVO(0, null, null,null);
        } else {
            for (UserOrderDO userOrderDO : list) {
                BikeDO bikeDO = slaveBikeMapper.selectByPk(userOrderDO.getBikeCode());
                String bikeNo = bikeDO.getBikeNo();
                if (userOrderDO.getOrderStatus().intValue() == ZebraConstant.USER_ORDER_STATUS_RIDING_COMMON) {
                    // todo fix tuantuan
                    // 判断行驶中的车辆是否是在临时停车状态
                    if (bikeDO.getBikeStatus() == ZebraConstant.BIKE_STATUS_12) {

                        OrderUnComplatedVO orderUnComplatedVO = new OrderUnComplatedVO(3, userOrderDO.getOrderId(), userOrderDO.getBikeCode(),bikeNo);
                        int ridingMins = DateUtil.getDiffMinites(userOrderDO.getStartTime());
                        //orderUnComplatedVO.setPriceDesc(dynamicParamConfig.getPriceDesc());
                        orderUnComplatedVO.setTimePrice(userOrderDO.getTotalAmount());
                        orderUnComplatedVO.setRidingDistAnce(userOrderDO.getTotalDistance());
                        orderUnComplatedVO.setMobiledTime(ridingMins);

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
                            orderUnComplatedVO.setPriceDesc(unit + "min/" + unitPrice + "￥");
                        }
                        return orderUnComplatedVO;
//                        return  new OrderUnComplatedVO(3, userOrderDO.getOrderId(), userOrderDO.getBikeCode());
                    }
                    // 车辆行驶中状态
                    return new OrderUnComplatedVO(1, userOrderDO.getOrderId(), userOrderDO.getBikeCode(),bikeNo);
                }
                if (userOrderDO.getOrderStatus().intValue() == ZebraConstant.USER_ORDER_STATUS_RIDING_END
                        && userOrderDO.getPayStatus().intValue() == ZebraConstant.USER_ORDER_PAY_STATUS_NO) {
                    logger.info("doJudgeUsersOrderSituation,uid={},userOrderDO={}", uid, userOrderDO);
                    OrderUnComplatedVO orderUnComplatedVO = new OrderUnComplatedVO(2, userOrderDO.getOrderId(), userOrderDO.getBikeCode(),bikeNo);
                    int ridingMins = DateUtil.getDiffMinites(userOrderDO.getStartTime(),userOrderDO.getEndTime());
                    orderUnComplatedVO.setPrice(userOrderDO.getTotalAmount());
                    orderUnComplatedVO.setPriceDesc("10min/2￥");
                    orderUnComplatedVO.setTimePrice(userOrderDO.getTotalAmount());
                    orderUnComplatedVO.setRidingDistAnce(userOrderDO.getTotalDistance());
                    orderUnComplatedVO.setMobiledTime(ridingMins);
                    return orderUnComplatedVO;
                }
            }
        }
        return null;
    }

    /**
     * 用户uid ,bikeCode，该车是否在行驶当中。 行驶当中为true 飞行驶当中为false
     *
     * @param uid
     * @param bikeCode
     * @return
     */
    @Override
    public UserOrderDO jugeBikeCurrentRidingStatus(Long uid, String bikeCode) {
        List<UserOrderDO> list = slaveUserOrderMapper.selectBikeCurrentRidingStatus(uid, bikeCode);
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

}
