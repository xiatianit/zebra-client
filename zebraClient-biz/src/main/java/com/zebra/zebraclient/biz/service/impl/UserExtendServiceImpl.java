package com.zebra.zebraclient.biz.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.biz.exception.ZebraclientException;
import com.zebra.zebraclient.biz.service.UserExtendService;
import com.zebra.zebraclient.dal.dataobject.UserExtendDO;
import com.zebra.zebraclient.dal.mapper.UserExtendMapper;

/**
 * 用户行程累计
 * 
 * @author xiatian
 * @version 2017年3月23日上午10:50:32
 */
@Service
public class UserExtendServiceImpl implements UserExtendService {
	private final static Logger logger = LoggerFactory.getLogger(UserExtendServiceImpl.class);

	@Autowired
	private UserExtendMapper masterUserExtendMapper;
	@Autowired
	private UserExtendMapper slaveUserExtendMapper;

	/**
	 * 新增用户行程累计
	 */
	@Override
	public void addUserExtend(UserExtendDO userExtendDO) {
		logger.info("新增用户累计行程，userExtendDO={}", JsonUtil.getJsonFromObject(userExtendDO));
		try {
			int effectRows = masterUserExtendMapper.insert(userExtendDO);
			if (effectRows == 0) {
				throw new ZebraclientException("新增用户累计行程失败");
			}
		} catch (Exception e) {
			logger.error("新增用户累计行程失败，userExtendDO={},e={}", JsonUtil.getJsonFromObject(userExtendDO), e);
			throw new ZebraclientException("新增用户累计行程失败");

		}
	}
   /**
    * 修改用户行程累计
    */
	@Override
	public void editUserExtend(UserExtendDO userExtendDO) {
		logger.info("修改用户累计行程，userExtendDO={}", JsonUtil.getJsonFromObject(userExtendDO));
		try {
			int effectRows = masterUserExtendMapper.update(userExtendDO);
			if (effectRows == 0) {
				throw new ZebraclientException("修改用户累计行程失败");
			}
		} catch (Exception e) {
			logger.error("修改用户累计行程失败，userExtendDO={},e={}", JsonUtil.getJsonFromObject(userExtendDO), e);
			throw new ZebraclientException("修改用户累计行程失败");
		}
	}
   /**
    * 查询用户行程累计
    */
	@Override
	public UserExtendDO queryUserExtendByUid(Long uid) {
		return slaveUserExtendMapper.selectByUid(uid);
	}

}
