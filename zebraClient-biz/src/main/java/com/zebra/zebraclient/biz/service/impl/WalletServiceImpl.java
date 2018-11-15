package com.zebra.zebraclient.biz.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zebra.common.constant.ZebraConstant;
import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.biz.exception.ZebraclientException;
import com.zebra.zebraclient.biz.service.WalletService;
import com.zebra.zebraclient.dal.dataobject.WalletDO;
import com.zebra.zebraclient.dal.mapper.WalletMapper;

/**
 * 押金信息
 * 
 * @author owen
 */
@Service
public class WalletServiceImpl implements WalletService {

    private final static Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    @Autowired
    private WalletMapper        masterWalletMapper;

    @Autowired
    private WalletMapper        slaveWalletMapper;

    /**
     * 新增押金信息
     */
    @Override
    public void addWallet(WalletDO walletDO) {
        logger.info("新增押金，walletDO={}", JsonUtil.getJsonFromObject(walletDO));
        try {
            int effectRows = masterWalletMapper.insert(walletDO);
            if (effectRows == 0) {
                throw new ZebraclientException("新增押金信息失败");
            }
        } catch (Exception e) {
            logger.error("新增押金信息失败，walletDO={},e={}", JsonUtil.getJsonFromObject(walletDO), e);
            throw new ZebraclientException("新增押金信息失败");
        }
    }

    /**
     * 修改押金信息
     */
    @Override
    public void editWallet(WalletDO walletDO) {
        logger.info("修改押金，walletDO={}", JsonUtil.getJsonFromObject(walletDO));
        try {
            int effectRows = masterWalletMapper.update(walletDO);
            if (effectRows == 0) {
                throw new ZebraclientException("修改押金信息失败");
            }
        } catch (Exception e) {
            logger.error("修改押金信息失败，walletDO={},e={}", JsonUtil.getJsonFromObject(walletDO), e);
            throw new ZebraclientException("修改押金信息失败");
        }
    }

    /**
     * 获取用户押金信息
     */
    @Override
    public WalletDO queryWalletByPk(Long uid) {
        return slaveWalletMapper.selectByPk(uid);
    }

    /**
     * 获取用户押金状态
     *
     * @param uid
     * @return
     */
    public int doGetWalletDepositStatus(Long uid) {
        WalletDO walletDO = slaveWalletMapper.selectByPk(uid);
        logger.info("walletDO={}", walletDO);
        if (walletDO == null) {
            // 该用户是第一次交互押金。
            return ZebraConstant.WALLET_DEPOSIT_STATUS_1;
        } else {
            return walletDO.getDepositStatus();
        }
    }
}
