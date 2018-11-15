package com.zebra.zebraclient.api.helper;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.biz.pojo.bean.UserToken;

/**
 * token工具类
 * @author owen
 *
 */
@Repository
public class TokenHelper {

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> valueOps;

    /**
     * 根据token 获取用户对象信息
     * @param token
     * @return
     */
    public UserToken getUidByToken(String token) {
        String userTokenStr = valueOps.get("token_" + token);
        if (!StringUtils.isEmpty(userTokenStr)) {
            return JsonUtil.getObjectFromJson(userTokenStr,UserToken.class);
        }
        return null;
    }
}
