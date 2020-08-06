package com.mj.shirotest.exception;

import org.apache.shiro.authc.AuthenticationException;

public class CustomException extends AuthenticationException {

    public CustomException() {
    }

    public CustomException(String message) {
        super(message);
    }

}
