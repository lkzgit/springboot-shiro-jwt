package com.mj.shirotest.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenUtil {

    //Token过期时间
    private static final long EXPIRE_TIME = 10 * 60 * 1000;
    private static final String TOKEN_SECRET = "shiro123";
    //当前时间与过期时间差小于这个时间，token刷新
    public static final long USED_TIME = 1 * 1000 * 60;

    /**
     * 生成签名
     * @param **username**
     * @param **password**
     * @return String
     */
    public static String sign(String username,Long userId) {
        try {
            // 设置过期时间
            // 私钥和加密算法
            // 设置头部信息
            Map<String, Object> header = new HashMap<>(2);
            header.put("Type", "Jwt");
            header.put("alg", "HS256");

//            正常Token：Token未过期，且未达到建议更换时间。
//            濒死Token：Token未过期，已达到建议更换时间。
//            正常过期Token：Token已过期，但存在于缓存中。
//            非正常过期Token：Token已过期，不存在于缓存中

            // 返回token字符串
            return JWT.create()
                    .withHeader(header)
                    .withClaim("userName", username)
                    .withClaim("userId",userId)
                    //过期时间
                    .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                    .sign(Algorithm.HMAC256(TOKEN_SECRET));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检验是否更新token
     */
    public static boolean verify(String token,String username,Long userId)throws JWTVerificationException {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                                    .withClaim("userName", username)
                                    .withClaim("userId",userId)
                                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
    }

    //获取token中的数据，不需要解密
    public static <T> T getField(String token,String field,Class<T> clazz) throws JWTDecodeException {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim(field).as(clazz);
    }

}
