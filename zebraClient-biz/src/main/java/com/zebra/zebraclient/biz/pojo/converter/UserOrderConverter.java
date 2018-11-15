package com.zebra.zebraclient.biz.pojo.converter;

import java.util.ArrayList;
import java.util.List;

import com.zebra.pony.util.DateUtil;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderDetailVO;
import com.zebra.zebraclient.biz.pojo.viewobject.OrderVO;
import com.zebra.zebraclient.dal.dataobject.UserOrderDO;

/**
 * 订单
 * 
 * @author owen
 *
 */
public class UserOrderConverter {

    public static OrderVO userOrderDO2OrderVO(UserOrderDO userOrderDO) {
        if (userOrderDO == null) {
            return null;
        }
        OrderVO orderVO = new OrderVO(userOrderDO.getOrderId(), userOrderDO.getStratAddress(), userOrderDO.getEndAddress(),
                DateUtil.formatyyyyMMddHHmm((long)userOrderDO.getStartTime()), userOrderDO.getTotalDistance(), userOrderDO.getTotalAmount());
        return orderVO;
    }

    public static OrderDetailVO userOrderDO2OrderDetailVO(UserOrderDO userOrderDO) {
        if (userOrderDO == null) {
            return null;
        }
        OrderDetailVO orderDetailVO = new OrderDetailVO(userOrderDO.getOrderId(), userOrderDO.getStratAddress(), userOrderDO.getEndAddress(),
                DateUtil.formatyyyyMMddHHmm((long)userOrderDO.getStartTime()), userOrderDO.getTotalDistance(), userOrderDO.getTotalAmount());
        return orderDetailVO;
    }

    public static List<OrderVO> userOrderDO2OrderVO(List<UserOrderDO> userOrderDOs) {
        if (userOrderDOs == null || userOrderDOs.size() == 0) {
            return null;
        }
        List<OrderVO> listVO = new ArrayList<OrderVO>(userOrderDOs.size());
        OrderVO orderVO = null;
        for (UserOrderDO userOrderDO : userOrderDOs) {
            orderVO = userOrderDO2OrderVO(userOrderDO);
            listVO.add(orderVO);
        }
        return listVO;
    }

}
