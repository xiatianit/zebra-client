package com.zebra.zebraclient.biz.pojo.converter;

import org.springframework.beans.BeanUtils;

import com.zebra.zebraclient.biz.pojo.viewobject.UserAddressVO;
import com.zebra.zebraclient.dal.dataobject.UserAddressDO;

/**
 * 用户地址转换
 *
 * @author owen
 *
 */
public class UserAddressConverter {

    public static UserAddressVO userDO2UserVO(UserAddressDO userAddressDO) {
        if (userAddressDO == null) {
            return null;
        }
        UserAddressVO userAddressVO = new UserAddressVO();
        BeanUtils.copyProperties(userAddressDO, userAddressVO);
        userAddressVO.setSetting(1);
        return userAddressVO;
    }

   

}
