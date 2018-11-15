package com.zebra.zebraclient.biz.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zebra.zebraclient.dal.dataobject.DeviceDO;

/**
 * 设备
 * 
 * @author owen
 */
public interface DeviceService {

    /**
     * 添加设备信息
     * 
     * @param deviceDO
     */
    void addDevice(DeviceDO deviceDO);

    /**
     * 修改设备信息
     * 
     * @param deviceDO
     */
    void editDevice(DeviceDO deviceDO);

    /**
     * 删除设备(通过UID)
     * 
     * @param uid
     */
    void delDeviceByUid(@Param("uid") Long uid);
    
    /**
     * 删除设备(通过主键)
     * 
     * @param uid
     */
    void delDeviceByPk(String deviceId);

    /**
     * 获取设备信息，根据主键ID
     * 
     * @param id
     * @return
     */
    DeviceDO queryDeviceByPk(String deviceId);

    /**
     * 根据UID来获取设备信息
     * 
     * @param uid
     * @return
     */
    List<DeviceDO> queryDeviceByUid(@Param("uid") Long uid);

}
