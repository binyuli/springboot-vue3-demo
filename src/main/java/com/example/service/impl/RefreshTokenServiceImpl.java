package com.example.service.impl;

import com.example.service.RefreshTokenService;
import com.example.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    
    @Override
    public String createRefreshToken(Long userId, String username) {
        String token = jwtUtil.generateRefreshToken(username);
        long expireTime = jwtUtil.getRefreshExpire();
        
        String key = REFRESH_TOKEN_PREFIX + userId;
        
        redisTemplate.opsForValue().set(key, token, expireTime, TimeUnit.MILLISECONDS);
        
        log.info("创建刷新令牌成功，用户: {}", username);
        
        return token;
    }
    
    @Override
    public String findByUserId(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("删除用户的刷新令牌，用户ID: {}", userId);
    }
    
    @Override
    public boolean validateToken(Long userId, String token) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = (String) redisTemplate.opsForValue().get(key);
        
        if (storedToken == null) {
            return false;
        }
        
        return storedToken.equals(token);
    }
}
