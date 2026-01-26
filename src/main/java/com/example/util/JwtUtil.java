package com.example.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于生成、验证和解析token
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT过期时间（毫秒）
     */
    @Value("${jwt.expire}")
    private long expire;

    /**
     * 刷新token过期时间（毫秒）
     */
    @Value("${jwt.refresh-expire}")
    private long refreshExpire;

    /**
     * 获取签名密钥
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            log.debug("使用Base64编码的JWT密钥");
        } catch (Exception e) {
            log.warn("JWT密钥不是有效的Base64编码，将使用明文密钥。建议配置Base64编码的32字节密钥。");
             keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        
        // 检查密钥长度，HS256至少需要256位（32字节）
        if (keyBytes.length < 32) {
            log.warn("JWT密钥长度不足32字节，可能会自动填充。建议使用32字节的密钥以确保安全。");
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT token
     * @param subject 主题，通常为用户名
     * @return JWT token
     */
    public String generateToken(String subject) {
        return generateToken(subject, new HashMap<>());
    }

    /**
     * 生成JWT token，带自定义声明
     * @param subject 主题，通常为用户名
     * @param claims 自定义声明
     * @return JWT token
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expire);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新token
     * @param subject 主题，通常为用户名
     * @return 刷新token
     */
    public String generateRefreshToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpire);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT token
     * @param token JWT token
     * @return Claims 对象
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("JWT解析异常: {}", e.getMessage());
            throw new IllegalArgumentException("无效的token");
        }
    }

    /**
     * 从token中获取用户名
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 验证token是否过期
     * @param token JWT token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * 验证token是否有效
     * @param token JWT token
     * @param username 用户名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return username.equals(tokenUsername) && !isTokenExpired(token);
    }

    public long getRefreshExpire() {
        return refreshExpire;
    }
}
