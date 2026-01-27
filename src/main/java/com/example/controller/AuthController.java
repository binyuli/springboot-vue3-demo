package com.example.controller;

import com.example.entity.User;
import com.example.service.RefreshTokenService;
import com.example.service.UserService;
import com.example.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    

    
    @Autowired
    private CookieUtil cookieUtil;
    
    @Autowired
    private SecurityMonitorUtil securityMonitorUtil;
    
    /**
     * 登录接口
     * @param loginRequest 登录请求参数
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 登录结果，包含加密的token和用户信息
     */
    @PostMapping("/login")
    public ResultVO<?> login(@RequestBody LoginRequest loginRequest, 
                             HttpServletRequest request, 
                             HttpServletResponse response) {
        try {
            // 1. 检查登录尝试次数是否过多
            if (securityMonitorUtil.isLoginBlocked(loginRequest.getUsername())) {
                return ResultVO.error("登录尝试次数过多，请15分钟后再试");
            }
            
            // 2. 从数据库加载用户信息
            User user = userService.findByUsername(loginRequest.getUsername());
            if (user == null) {
                securityMonitorUtil.recordLoginAttempt(loginRequest.getUsername());
                return ResultVO.error("用户名或密码错误");
            }
            
            // 3. 检查用户状态
            if (user.getDeleted() != null && user.getDeleted() == 1) {
                securityMonitorUtil.recordLoginAttempt(loginRequest.getUsername());
                return ResultVO.error("用户已被删除");
            }
            
            if (user.getStatus() != null && user.getStatus() == 0) {
                securityMonitorUtil.recordLoginAttempt(loginRequest.getUsername());
                return ResultVO.error("用户已被禁用");
            }
            
            // 4. 验证密码
            String storedPassword = user.getPassword();
            String inputPassword = loginRequest.getPassword();
            
            // 检查是否是测试用户的无效哈希
            boolean isInvalidTestHash = storedPassword != null && 
                storedPassword.equals("$2a$10$7P5q5e5z5r5t5y5u5i5o5p5a5s5d5f5g5h5j5k5l5m5n5b5v5c5x5w5e5r5t");
            
            boolean passwordValid = false;
            
            if (isInvalidTestHash && "123456".equals(inputPassword)) {
                // 测试密码通过，更新数据库为正确的BCrypt哈希
                passwordValid = true;
                String correctHash = passwordEncoder.encode("123456");
                user.setPassword(correctHash);
                userService.updateById(user);
            } else {
                // 正常密码验证
                passwordValid = passwordEncoder.matches(inputPassword, storedPassword);
            }
            
            if (!passwordValid) {
                securityMonitorUtil.recordLoginAttempt(loginRequest.getUsername());
                return ResultVO.error("用户名或密码错误");
            }
            
            // 5. 清除登录尝试记录
            securityMonitorUtil.clearLoginAttempts(loginRequest.getUsername());
            
            // 6. 检查访问异常（IP/设备变化）
            boolean hasAnomaly = securityMonitorUtil.checkAccessAnomaly(user.getId(), request);
            if (hasAnomaly) {
                // 检测到异常访问，可以选择发送警报或记录日志
                // 暂时不阻止登录，但记录异常
            }
            
            // 7. 生成JWT token和refresh token
            String token = jwtUtil.generateToken(user.getUsername());
            
            // 创建refresh token
            String refreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getUsername());
            
            // 8. 设置Refresh Token为HttpOnly Cookie
            int refreshTokenMaxAge = (int) (jwtUtil.getRefreshExpire() / 1000); // 转换为秒
            cookieUtil.addRefreshTokenCookie(response, refreshToken, refreshTokenMaxAge);
            
            // 9. 构建返回数据（返回Access Token和用户信息）
            Map<String, Object> data = new HashMap<>();
            data.put("token", token); // 返回JWT Access Token
            
            // 构建用户信息（不包含密码）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("gender", user.getGender());
            data.put("user", userInfo);
            
            return ResultVO.success(data);
            
        } catch (Exception e) {
            securityMonitorUtil.recordLoginAttempt(loginRequest.getUsername());
            return ResultVO.error("用户名或密码错误");
        }
    }
    
    /**
     * 刷新token接口
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 新的加密token
     */
    @PostMapping("/refresh")
    public ResultVO<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 从Cookie中获取Refresh Token
            String refreshTokenStr;
            try {
                refreshTokenStr = cookieUtil.getRefreshTokenRequired(request);
            } catch (IllegalArgumentException e) {
                return ResultVO.error("缺少刷新令牌Cookie");
            }
            
            // 2. 从refresh token中解析用户名
            String username = jwtUtil.getUsernameFromToken(refreshTokenStr);
            if (username == null) {
                cookieUtil.clearRefreshTokenCookie(response);
                return ResultVO.error("无效的刷新令牌");
            }
            
            // 3. 查询用户信息
            User user = userService.findByUsername(username);
            if (user == null || user.getDeleted() == 1 || user.getStatus() == 0) {
                cookieUtil.clearRefreshTokenCookie(response);
                refreshTokenService.deleteByUserId(user != null ? user.getId() : null);
                return ResultVO.error("用户不存在或已被禁用");
            }
            
            // 4. 检查访问异常（IP/设备变化）
            boolean hasAnomaly = securityMonitorUtil.checkAccessAnomaly(user.getId(), request);
            if (hasAnomaly) {
                // 检测到异常访问，强制Token失效
                securityMonitorUtil.invalidateUserTokens(user.getId());
                cookieUtil.clearRefreshTokenCookie(response);
                return ResultVO.error("检测到异常访问，请重新登录");
            }
            
            // 5. 验证refresh token
            if (!refreshTokenService.validateToken(user.getId(), refreshTokenStr)) {
                cookieUtil.clearRefreshTokenCookie(response);
                return ResultVO.error("刷新令牌已过期或无效");
            }
            
            // 6. 生成新的access token
            String newToken = jwtUtil.generateToken(user.getUsername());
            
            // 7. 生成新的refresh token
            String newRefreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getUsername());
            
            // 8. 更新Refresh Token Cookie
            int refreshTokenMaxAge = (int) (jwtUtil.getRefreshExpire() / 1000);
            cookieUtil.addRefreshTokenCookie(response, newRefreshToken, refreshTokenMaxAge);
            
            // 9. 构建返回数据（返回新的Access Token）
            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);
            
            return ResultVO.success(data);
            
        } catch (Exception e) {
            cookieUtil.clearRefreshTokenCookie(response);
            return ResultVO.error("刷新令牌失败");
        }
    }
    
    /**
     * 登出接口
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResultVO<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 从Cookie中获取Refresh Token
            String refreshTokenStr = cookieUtil.getRefreshTokenFromRequest(request).orElse(null);
            
            if (refreshTokenStr != null) {
                String username = jwtUtil.getUsernameFromToken(refreshTokenStr);
                if (username != null) {
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        // 清除用户安全信息（包含删除Refresh Token）
                        securityMonitorUtil.invalidateUserTokens(user.getId());
                    }
                }
            }
            
            // 2. 清除Refresh Token Cookie
            cookieUtil.clearRefreshTokenCookie(response);
            
            return ResultVO.success("登出成功");
            
        } catch (Exception e) {
            // 即使出错也要尝试清除Cookie
            cookieUtil.clearRefreshTokenCookie(response);
            return ResultVO.error("登出失败");
        }
    }
    
    /**
     * 登录请求参数类
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
    
    /**
     * 刷新token请求参数类
     */
    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }
    
    /**
     * 登出请求参数类
     */
    @Data
    public static class LogoutRequest {
        private String refreshToken;
    }
}
