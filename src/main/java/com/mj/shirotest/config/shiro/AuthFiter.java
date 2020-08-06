package com.mj.shirotest.config.shiro;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mj.shirotest.constant.ShiroConstant;
import com.mj.shirotest.entity.UserEntity;
import com.mj.shirotest.exception.CustomException;
import com.mj.shirotest.util.RedisUtil;
import com.mj.shirotest.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AuthFiter extends AuthenticatingFilter {

    /**
     * 生成自定义token
     */
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        //获取请求token
        String token = getRequestToken((HttpServletRequest) request);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return new AuthToken(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {

        AuthToken jwtToken = (AuthToken)this.createToken(request,response);
        if(jwtToken != null){
            String token = jwtToken.getToken();

//            try {
                // 提交给realm进行登入，如果错误他会抛出异常并被捕获
                // 如果没有抛出异常则代表登入成功，返回true
                getSubject(request, response).login(jwtToken);
//            }catch (AuthenticationException e){
                //获取异常并输出
//                this.customResponse(e.getMessage(),response);
//                return false;
//            }

            //判断是否要更新token
            String refreshToken = this.refreshToken(token);
            if(!StringUtils.isEmpty(refreshToken)){
                log.info("更新token时间！！！！！");
                UserEntity user = (UserEntity) SecurityUtils.getSubject().getPrincipal();
                user.setToken(refreshToken);
                RedisUtil.set(ShiroConstant.LOGIN_SHIRO_CACHE + user.getId(),user);
                HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
                httpServletResponse.setHeader("token", refreshToken);
                httpServletResponse.setHeader("Access-Control-Expose-Headers", "token");
            }
            return true;
        }else{
            this.customResponse("token不能为空", response);
            return false;
        }
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }

    /**
     * 获取请求的token
     */
    private String getRequestToken(HttpServletRequest httpRequest) {
        //从header中获取token
        String token = httpRequest.getHeader("token");
        //如果header中不存在token，则从参数中获取token
        if (StringUtils.isEmpty(token)) {
            token = httpRequest.getParameter("token");
        }
        return token;
    }

    /**
     * 更新token
     */
    private String refreshToken(String token){

        String sign = null;
        DecodedJWT jwt = JWT.decode(token);
        //获取过期时间
        Date exDate = jwt.getExpiresAt();

        //比较过期时间
        boolean refesh = (exDate.getTime() - System.currentTimeMillis()) < TokenUtil.USED_TIME;
        if(refesh){
            //获取token中的数据
            Long userId = TokenUtil.getField(token,"userId",Long.class);
            String userName = TokenUtil.getField(token,"userName",String.class);
            sign = TokenUtil.sign(userName,userId);
        }

       return sign;
    }

    private void customResponse(String msg,ServletResponse response){
        try {
            Map<String,Object> map = new HashMap<>();
            response.setContentType(MediaType.APPLICATION_JSON_UTF8.toString());
            map.put("code",405);
            map.put("msg",msg);
            String resultJson= JSON.toJSONString(map);
            OutputStream out=response.getOutputStream();
            out.write(resultJson.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
