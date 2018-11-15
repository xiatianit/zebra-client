package com.zebra.zebraclient.biz.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.biz.exception.ZebraclientException;
import com.zebra.zebraclient.biz.service.UserAddressService;
import com.zebra.zebraclient.dal.dataobject.UserAddressDO;
import com.zebra.zebraclient.dal.mapper.UserAddressMapper;

/**
 * 电池服务信息
 * 
 * @author owen
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    private final static Logger logger = LoggerFactory.getLogger(UserAddressServiceImpl.class);

    @Autowired
    private UserAddressMapper   masterUserAddressMapper;

    @Autowired
    private UserAddressMapper   slaveUserAddressMapper;

    /**
     * 新增常用地址信息
     */
    @Override
    public void addUserAddress(UserAddressDO userAddressDO) {
        logger.info("新增常用地址信息，userAddressDO={}", userAddressDO);
        try {
            UserAddressDO tempUserAddressDO = slaveUserAddressMapper.selectByAddTypeAndUid(userAddressDO.getUid(), userAddressDO.getAddressType());
            if (tempUserAddressDO == null) {
                masterUserAddressMapper.insert(userAddressDO);
            } else {
                userAddressDO.setId(tempUserAddressDO.getId());
                masterUserAddressMapper.update(userAddressDO);
            }
        } catch (Exception e) {
            logger.error("新增常用地址信息失败，userAddressDO={},e={}", JsonUtil.getJsonFromObject(userAddressDO), e);
            throw new ZebraclientException("新增常用地址信息失败");
        }
    }

    /**
     * 删除
     * 
     * @param uid
     * @param addressType
     */
    @Override
    public void deleteUserAddressByAddTypeAndUid(Long uid, Integer addressType) {
        logger.info("删除常用地址信息，uid={},addressType={}", uid, addressType);
        try {
            masterUserAddressMapper.deleteByAddTypeAndUid(uid, addressType);
        } catch (Exception e) {
            logger.error("删除常用地址信息失败，，uid={},addressType={},e={}", uid, addressType, e);
            throw new ZebraclientException("删除常用地址信息失败");
        }
    }

    @Override
    public UserAddressDO queryUserAddressByAddTypeAndUid(Long uid, Integer addressType) {
        logger.info("获取常用地址信息，uid={},addressType={}", uid, addressType);
        return slaveUserAddressMapper.selectByAddTypeAndUid(uid, addressType);
    }

    @Override
    public List<UserAddressDO> queryUserAddressByCondition(UserAddressDO userAddressDO) {
        logger.info("新增常用地址信息，userAddressDO={}", userAddressDO);
        return slaveUserAddressMapper.selectByCondition(userAddressDO);
    }

}
