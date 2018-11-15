package com.zebra.zebraclient.biz.service.impl;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.biz.exception.ZebraclientException;
import com.zebra.zebraclient.biz.service.DeviceService;
import com.zebra.zebraclient.dal.dataobject.DeviceDO;
import com.zebra.zebraclient.dal.mapper.DeviceMapper;

/**
 * 电池服务信息
 * 
 * @author owen
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    private final static Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);

    @Autowired
    private DeviceMapper        masterDeviceMapper;

    @Autowired
    private DeviceMapper        slaveDeviceMapper;

    /**
     * 添加设备
     */
    @Override
    public void addDevice(DeviceDO deviceDO) {
        logger.info("新增设备信息，deviceDO={}", JsonUtil.getJsonFromObject(deviceDO));
        try {
            int effectRows = masterDeviceMapper.insert(deviceDO);
            if (effectRows == 0) {
                throw new ZebraclientException("新增设备失败");
            }
        } catch (Exception e) {
            logger.error("新增设备信息失败，deviceDO={},e={}", JsonUtil.getJsonFromObject(deviceDO), e);
            throw new ZebraclientException("新增设备信息失败");
        }
    }

    /**
     * 修改设备
     */
    @Override
    public void editDevice(DeviceDO deviceDO) {
        logger.info("修改设备信息，deviceDO={}", JsonUtil.getJsonFromObject(deviceDO));
        try {
            int effectRows = masterDeviceMapper.update(deviceDO);
            if (effectRows == 0) {
                throw new ZebraclientException("修改设备失败");
            }
        } catch (Exception e) {
            logger.error("修改设备信息失败，deviceDO={},e={}", JsonUtil.getJsonFromObject(deviceDO), e);
            throw new ZebraclientException("修改设备信息失败");
        }
    }

    /**
     * 删除设备(通过UID)
     * 
     * @param uid
     */
    @Override
    public void delDeviceByUid(Long uid) {
        logger.info("删除设备信息，uid={}", JsonUtil.getJsonFromObject(uid));
        try {
            masterDeviceMapper.deleteDeviceByUid(uid);
        } catch (Exception e) {
            logger.error("删除设备信息，uid={},e={}", JsonUtil.getJsonFromObject(uid), e);
            throw new ZebraclientException("删除设备信息失败");
        }
    }

    /**
     * 删除设备(通过主键)
     * 
     * @param uid
     */
    @Override
    public void delDeviceByPk(String deviceId) {
        logger.info("删除设备信息，deviceId={}", deviceId);
        try {
            masterDeviceMapper.deleteDeviceByPk(deviceId);
        } catch (Exception e) {
            logger.error("删除设备信息，deviceId={},e={}", deviceId, e);
            throw new ZebraclientException("删除设备信息失败");
        }

    }

    /**
     * 查询设备
     */
    @Override
    public DeviceDO queryDeviceByPk(String deviceId) {
        return slaveDeviceMapper.selectByPk(deviceId);
    }

    /**
     * 根据UID来获取设备信息
     * 
     * @param uid
     * @return
     */
    @Override
    public List<DeviceDO> queryDeviceByUid(Long uid) {
        return slaveDeviceMapper.selectByUid(uid);
    }

}
