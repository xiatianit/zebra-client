package com.zebra.zebraclient.biz.pojo.converter;

import org.springframework.beans.BeanUtils;

import com.zebra.zebraclient.biz.pojo.viewobject.UserDetailVO;
import com.zebra.zebraclient.dal.dataobject.UserBaseDO;

/**
 * 员工转换
 * 
 * @author owen
 *
 */
public class UserConverter {

    public static UserDetailVO userDO2UserVO(UserBaseDO userBaseDO) {
        if (userBaseDO == null) {
            return null;
        }
        UserDetailVO userDetailVO = new UserDetailVO();
        BeanUtils.copyProperties(userBaseDO, userDetailVO);
        return userDetailVO;
    }

   

}
