package com.example.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 安全监控工具类
 * 监控IP、设备变化，检测异常访问
 */
@Slf4j
@Component
public class SecurityMonitorUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户安全信息前缀
     */
    private static final String USER_SECURITY_PREFIX = "user_security:";

    /**
     * 登录尝试前缀（防暴力破解）
     */
    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";

    /**
     * 最大登录尝试次数
     */
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 登录尝试锁定时间（分钟）
     */
    private static final int LOGIN_LOCK_TIME_MINUTES = 15;

    /**
     * 获取客户端真实IP地址
     * @param request HTTP请求
     * @return 客户端真实IP
     */
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果有多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 获取客户端用户代理
     * @param request HTTP请求
     * @return 用户代理字符串
     */
    public String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }

    /**
     * 生成设备指纹（基于IP和User-Agent的简单哈希）
     * @param request HTTP请求
     * @return 设备指纹
     */
    public String generateDeviceFingerprint(HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = getUserAgent(request);
        String fingerprint = ip + "|" + userAgent;
        return Integer.toHexString(fingerprint.hashCode());
    }

    /**
     * 检查用户登录/访问是否有异常（IP或设备变化）
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 是否检测到异常访问
     */
    public boolean checkAccessAnomaly(Long userId, HttpServletRequest request) {
        try {
            String currentIp = getClientIp(request);
            String currentDeviceFingerprint = generateDeviceFingerprint(request);
            
            String key = USER_SECURITY_PREFIX + userId;
            Map<Object, Object> securityInfo = redisTemplate.opsForHash().entries(key);
            
            if (securityInfo.isEmpty()) {
                // 首次登录，保存安全信息
                saveSecurityInfo(userId, currentIp, currentDeviceFingerprint);
                return false;
            }
            
            String lastIp = (String) securityInfo.get("last_ip");
            String lastDeviceFingerprint = (String) securityInfo.get("last_device_fingerprint");
            
            boolean ipChanged = lastIp != null && !lastIp.equals(currentIp);
            boolean deviceChanged = lastDeviceFingerprint != null && !lastDeviceFingerprint.equals(currentDeviceFingerprint);
            
            if (ipChanged || deviceChanged) {
                log.warn("检测到异常访问，用户ID: {}，IP变化: {} -> {}，设备变化: {} -> {}", 
                        userId, lastIp, currentIp, lastDeviceFingerprint, currentDeviceFingerprint);
                
                // 记录异常访问
                recordAnomalyAccess(userId, lastIp, currentIp, lastDeviceFingerprint, currentDeviceFingerprint);
                
                // 更新为新的安全信息
                saveSecurityInfo(userId, currentIp, currentDeviceFingerprint);
                
                return true;
            }
            
            // 更新最后访问时间
            updateLastAccessTime(userId);
            
            return false;
        } catch (Exception e) {
            log.error("检查访问异常失败，用户ID: {}", userId, e);
            return false;
        }
    }

    /**
     * 保存用户安全信息
     * @param userId 用户ID
     * @param ip IP地址
     * @param deviceFingerprint 设备指纹
     */
    private void saveSecurityInfo(Long userId, String ip, String deviceFingerprint) {
        String key = USER_SECURITY_PREFIX + userId;
        Map<String, String> securityInfo = new HashMap<>();
        securityInfo.put("last_ip", ip);
        securityInfo.put("last_device_fingerprint", deviceFingerprint);
        securityInfo.put("last_access_time", String.valueOf(System.currentTimeMillis()));
        
        redisTemplate.opsForHash().putAll(key, securityInfo);
        redisTemplate.expire(key, Duration.ofDays(30)); // 保存30天
    }

    /**
     * 更新最后访问时间
     * @param userId 用户ID
     */
    private void updateLastAccessTime(Long userId) {
        String key = USER_SECURITY_PREFIX + userId;
        redisTemplate.opsForHash().put(key, "last_access_time", String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 记录异常访问
     * @param userId 用户ID
     * @param lastIp 上次IP
     * @param currentIp 当前IP
     * @param lastDevice 上次设备
     * @param currentDevice 当前设备
     */
    private void recordAnomalyAccess(Long userId, String lastIp, String currentIp, 
                                     String lastDevice, String currentDevice) {
        String anomalyKey = "anomaly_access:" + userId + ":" + System.currentTimeMillis();
        Map<String, String> anomalyInfo = new HashMap<>();
        anomalyInfo.put("user_id", String.valueOf(userId));
        anomalyInfo.put("last_ip", lastIp);
        anomalyInfo.put("current_ip", currentIp);
        anomalyInfo.put("last_device", lastDevice);
        anomalyInfo.put("current_device", currentDevice);
        anomalyInfo.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        redisTemplate.opsForHash().putAll(anomalyKey, anomalyInfo);
        redisTemplate.expire(anomalyKey, Duration.ofDays(90)); // 保存90天用于审计
    }

    /**
     * 检查登录尝试次数（防暴力破解）
     * @param username 用户名
     * @return 是否超过最大尝试次数
     */
    public boolean isLoginBlocked(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        return attempts != null && attempts >= MAX_LOGIN_ATTEMPTS;
    }

    /**
     * 记录登录尝试失败
     * @param username 用户名
     */
    public void recordLoginAttempt(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        
        if (attempts == null) {
            attempts = 1;
        } else {
            attempts++;
        }
        
        redisTemplate.opsForValue().set(key, attempts, LOGIN_LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            log.warn("用户 {} 登录尝试次数过多，已被锁定 {} 分钟", username, LOGIN_LOCK_TIME_MINUTES);
        }
    }

    /**
     * 清除登录尝试记录（登录成功时调用）
     * @param username 用户名
     */
    public void clearLoginAttempts(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        redisTemplate.delete(key);
    }

    /**
     * 强制使某个用户的所有Token失效（发现异常时调用）
     * @param userId 用户ID
     */
    public void invalidateUserTokens(Long userId) {
        try {
            // 删除用户的Refresh Token
            String refreshTokenKey = "refresh_token:" + userId;
            redisTemplate.delete(refreshTokenKey);
            
            // 删除用户安全信息
            String securityKey = USER_SECURITY_PREFIX + userId;
            redisTemplate.delete(securityKey);
            
            log.warn("已强制使用户 {} 的所有Token失效", userId);
        } catch (Exception e) {
            log.error("强制使用户Token失效失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 获取用户最后一次正常访问的信息
     * @param userId 用户ID
     * @return 安全信息Map
     */
    public Map<Object, Object> getUserSecurityInfo(Long userId) {
        String key = USER_SECURITY_PREFIX + userId;
        return redisTemplate.opsForHash().entries(key);
    }
}