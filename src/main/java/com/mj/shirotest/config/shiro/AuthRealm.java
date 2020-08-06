package com.mj.shirotest.config.shiro;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.mj.shirotest.constant.ShiroConstant;
import com.mj.shirotest.entity.PermissionEntity;
import com.mj.shirotest.entity.UserEntity;
import com.mj.shirotest.exception.CustomException;
import com.mj.shirotest.repository.PermissionRepsitory;
import com.mj.shirotest.repository.UserRepository;
import com.mj.shirotest.util.RedisUtil;
import com.mj.shirotest.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class AuthRealm extends AuthorizingRealm {

    @Autowired
    private PermissionRepsitory permissionRepsitory;
    @Autowired
    private UserRepository userRepository;

    //必须重写，不然会报错
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof AuthToken;
    }

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        log.debug("开始执行授权操作.......");

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        //如果身份认证的时候没有传入User对象，这里只能取到userName
        //也就是SimpleAuthenticationInfo构造的时候第一个参数传递需要User对象
        UserEntity user = (UserEntity) principalCollection.getPrimaryPrincipal();
        Long userId = user.getId();

        //获取权限并设置
        List<PermissionEntity> list = permissionRepsitory.findByUserId(userId);
        if(!list.isEmpty()){
            list.forEach(o ->{
                authorizationInfo.addStringPermission(o.getRoleId().toString());
            });
        }

        return authorizationInfo;
    }

    //验证用户
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {


        log.info("验证开始。。。");
        String token = (String) authenticationToken.getCredentials();
        UserEntity user;

        try{
            //获取token中的userId
            Long userId = TokenUtil.getField(token,"userId",Long.class);
            if(!RedisUtil.hasKey(ShiroConstant.LOGIN_SHIRO_CACHE + userId)){
                throw new CustomException("redis无该用户,登出或被删除,请重新登陆!");
            }

            user = (UserEntity) RedisUtil.get(ShiroConstant.LOGIN_SHIRO_CACHE + userId);
            if(!user.getToken().equals(token)){
                throw new CustomException("token不等错误！请重新登陆");
            }
            //token验证
            TokenUtil.verify(token,user.getUserName(),user.getId());
        }catch (JWTDecodeException e){
            throw new UnsupportedTokenException("token解密错误");
        }catch (TokenExpiredException e){
            throw new UnsupportedTokenException("token许可时间过期",e);
        }catch (JWTVerificationException e){
            throw new UnsupportedTokenException("token解析错误");
        }

        return new SimpleAuthenticationInfo(user, token, this.getName());
    }

    /**
     * 重写 获取缓存名
     */
    @Override
    public String getAuthorizationCacheName() {
        return ShiroConstant.ROLE_SHIRO_CACHE.substring(0,ShiroConstant.ROLE_SHIRO_CACHE.length() - 1);
    }
}
