package com.zebra.zebraclient.biz.pojo.viewobject;

import java.io.Serializable;

/**
 * token对象
 * 
 * @author owen
 *
 */
public class TokenStrVO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String            token;

    public TokenStrVO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
