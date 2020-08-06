package com.mj.shirotest.config;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ErrorController extends BasicErrorController {

    /**
     * 必须实现的一个构造方法
     **/
    public ErrorController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    /**
     * produces 设置返回的数据类型：application/json
     *
     * @param request 请求
     * @return 自定义的返回实体类
     */
    @Override
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
        HttpStatus status = getStatus(request);
        // 获取错误信息
        String message = body.get("message").toString();

        return new ResponseEntity<>(new ResultMap(400,message), status);
    }
}

class ResultMap extends HashMap<String, Object> {

    /**
     * 状态码
     */
    private String status;
    /**
     * 返回内容
     */
    private String message;

    public ResultMap() {
    }

    /**
     * 初始化一个新创建的 Result 对象
     * @param status
     * @param message
     */
    public ResultMap(Integer status, Object message) {
        this.put("status", status);
        this.put("message", message);
    }

}
