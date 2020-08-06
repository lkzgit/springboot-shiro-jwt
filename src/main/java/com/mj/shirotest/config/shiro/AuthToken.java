package com.mj.shirotest.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class AuthToken implements AuthenticationToken {

    private String token;

    public AuthToken() {
    }

    public AuthToken(String token) {
        this.token = token;
    }
//
    public String getToken() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;

    }
}
