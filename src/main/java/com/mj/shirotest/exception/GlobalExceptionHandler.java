package com.mj.shirotest.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@ControllerAdvice
@ResponseBody
@Component
public class GlobalExceptionHandler {

    /**
     * @param  ex
     * @Description: 运行时异常
     */
    @ExceptionHandler(CustomException.class)
    public Map<String,String> CustomExceptionHandler(CustomException ex) {
        log.error(ex.getMessage(), ex);
        Map<String,String> result = new HashMap<>();
        result.put("msg",ex.getMessage());
        result.put("code","400");
        return result;
    }

    /**
     * @param  ex
     * @Description: 权限认证异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    public Map<String,String> unauthorizedExceptionHandler(Exception ex) {
        log.error(ex.getMessage(), ex);
        Map<String,String> result = new HashMap<>();
        result.put("msg",ex.getMessage());
        result.put("code","400");
        result.put("obj","权限不够");
        return result;
    }
}
