package com.wang.back.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
//JWT = JSON Web Token
//toke的产生与使用
public class JwtUtil {

    //密钥                                //填上你自己的密钥，>=32字节,如f7Kpz9Wq3LmVx2RtYh8Bn4Jc6Dg5Sf1Z
    private static final String SECRET = "密钥";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    //Token有效期：1天
    private static final long EXPIRATION = 1 * 24 * 60 * 60 * 1000L;

    //签发token
    public String generateToken(Long userId, String username) {
        //构建自定义载荷
        Map<String, Object> claims = new HashMap<>();
        //载荷是JWT里存自定义数据的地方
        claims.put("userId", userId);
        claims.put("username", username);
        //构建JWT
        return Jwts.builder()
                .claims(claims)     //放入自定义载荷
                .issuedAt(new Date())   //记录签发时间
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))  //设置过期时间
                .signWith(KEY)  //用密钥签名，防止篡改
                .compact();     //把整个JWT压缩成字符串
    }

    //解析Token
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)    //指定验证签名的密钥
                .build()    //构建解析器
                .parseSignedClaims(token)   //解析token并验证签名、有效期
                .getPayload();  //取出载荷（claims对象）
    }

    //从Token里取userId
    public Long getUserId(String token) {
        Claims claims = parseToken(token);  //解析token
        return claims.get("userId", Long.class);    //从Claims里取出userId字段，转成Long
    }
}