package com.example.service;

public interface RefreshTokenService {
    
    String createRefreshToken(Long userId, String username);
    
    String findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    boolean validateToken(Long userId, String token);
}

