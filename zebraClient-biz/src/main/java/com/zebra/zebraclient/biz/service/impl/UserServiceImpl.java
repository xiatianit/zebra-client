package com.zebra.zebraclient.biz.service.impl;

import com.gexin.fastjson.JSONObject;
import com.zebra.pony.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zebra.pony.util.JsonUtil;
import com.zebra.zebraclient.biz.exception.ZebraclientException;
import com.zebra.zebraclient.biz.service.UserService;
import com.zebra.zebraclient.dal.dataobject.UserBaseDO;
import com.zebra.zebraclient.dal.mapper.UserBaseMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户
 *
 * @author owen
 */
@Service
public class UserServiceImpl implements UserService {

    private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserBaseMapper masterUserBaseMapper;

    @Autowired
    private UserBaseMapper slaveUserBaseMapper;

    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

    //配置您申请的KEY
    public static final String APPKEY = "8af21fadf59f9545c263ae25144fdb04";


    /****************** 基础的增删改查 ********************/
    /**
     * 查询
     *
     * @param
     * @return
     */
    public UserBaseDO queryByPk(Long uid) {
        return slaveUserBaseMapper.selectByPk(uid);
    }

    /**
     * 查询
     *
     * @param
     * @return
     */
    public UserBaseDO queryByPhone(String phone) {
        return slaveUserBaseMapper.selectByPhone(phone);
    }

    /**
     * 查询,关联信息也查出来
     *
     * @param
     * @return
     */
    public UserBaseDO queryRelByPk(Long uid) {
        return slaveUserBaseMapper.selectRelByPk(uid);
    }

    /**
     * 插入
     *
     * @param userBaseDO
     * @return
     */
    public void addUserBase(UserBaseDO userBaseDO) {
        logger.info("新增用户，userBaseDO={}", JsonUtil.getJsonFromObject(userBaseDO));
        try {
            masterUserBaseMapper.insert(userBaseDO);
        } catch (Exception e) {
            logger.error("新增用户失败，userBaseDO={}，e={}", JsonUtil.getJsonFromObject(userBaseDO), e);
            throw new ZebraclientException("新增用户失败");
        }
    }

    /**
     * 更新
     *
     * @param userBaseDO
     * @return
     */
    public void editUserBase(UserBaseDO userBaseDO) {
        logger.info("修改用户，userBaseDO={}", JsonUtil.getJsonFromObject(userBaseDO));
        try {
            masterUserBaseMapper.update(userBaseDO);
        } catch (Exception e) {
            logger.error("修改用户失败，userBaseDO={}，e={}", JsonUtil.getJsonFromObject(userBaseDO), e);
            throw new ZebraclientException("修改用户失败");
        }
    }

    @Override
    public String doRealName(Long uid, String realName, String idcard) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("realname", realName);
        m.put("idcard", idcard);
        m.put("key", APPKEY);
        String result;
        try {
            result = net("http://op.juhe.cn/idcard/query", m, "POST");
            logger.info("调实名认证接口返回结果{}", JsonUtil.getJsonFromObject(result));
            JSONObject object = JSONObject.parseObject(result);
            String errorCode = object.getString("error_code").toString();
            String reason = object.getString("reason").toString();
            if (!errorCode.equals("0")) {
                return reason;
            }
            String[] b = result.split("\"res\":");
            //姓名身份证匹配更新用户
            if (b.length > 0 && Integer.parseInt(b[1].substring(0, 1)) == 1) {
                UserBaseDO userBaseDO = slaveUserBaseMapper.selectByPk(uid);
                userBaseDO.setActualName(realName);
                userBaseDO.setIdCardNo(idcard);
                userBaseDO.setVerifyStatus(1);
                userBaseDO.setModifyTime(DateUtil.getCurrentTimeSeconds());
                masterUserBaseMapper.update(userBaseDO);
                return "success";
            }
        } catch (Exception e) {
            logger.error("实名认证调接口异常e={}", e);
            return "实名认证失败";
        }
        return "实名认证失败";
    }


    /**
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return 网络请求字符串
     * @throws Exception
     */
    public static String net(String strUrl, Map params, String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if (method == null || method.equals("GET")) {
                strUrl = strUrl + "?" + urlencode(params);
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if (method == null || method.equals("GET")) {
                conn.setRequestMethod("GET");
            } else {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params != null && method.equals("POST")) {
                try {
                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes(urlencode(params));
                } catch (Exception e) {
                    logger.error("调接口异常e={}", e);
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型
    public static String urlencode(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


}
