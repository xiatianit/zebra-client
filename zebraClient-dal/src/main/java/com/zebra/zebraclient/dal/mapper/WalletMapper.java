package com.zebra.zebraclient.dal.mapper;

import org.apache.ibatis.annotations.Param;

import com.zebra.zebraclient.dal.dataobject.WalletDO;

public interface WalletMapper {
    
    /******************基础的增删改查********************/
    /**
     * 查询
     *
     * @param id
     * @return
     */
    WalletDO selectByPk(@Param("uid") Long uid);

    /**
     * 插入
     *
     * @param walletDO
     * @return
     */
    int insert(WalletDO walletDO);

    /**
     * 更新
     *
     * @param walletDO
     * @return
     */
    int update(WalletDO walletDO);

}
