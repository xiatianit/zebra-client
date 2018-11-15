package com.zebra.zebraclient.biz.service;

import com.zebra.zebraclient.dal.dataobject.WalletDO;

/**
 * 用户押金
 * 
 * @author owen
 */
public interface WalletService {

    /**
     * 添加用户押金信息
     * 
     * @param
     */
    void addWallet(WalletDO walletDO);

    /**
     * 修改用户押金信息
     * 
     * @param
     */
    void editWallet(WalletDO walletDO);

    /**
     * 获取用户押金信息，根据主键 uid
     * 
     * @param uid
     * @return
     */
    WalletDO queryWalletByPk(Long uid);

    /**
     * 获取用户押金状态
     *
     * @param uid
     * @return
     */
    int doGetWalletDepositStatus(Long uid);

    

}
