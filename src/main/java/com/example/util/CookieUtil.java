package com.example.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Cookie工具类
 * 用于安全地管理HttpOnly、Secure、SameSite的Cookie
 */
@Slf4j
@Component
public class CookieUtil {

    /**
     * 是否启用HTTPS（生产环境应为true）
     */
    @Value("${app.secure:false}")
    private boolean secure;

    /**
     * Cookie域名
     */
    @Value("${app.cookie-domain:}")
    private String cookieDomain;

    /**
     * Refresh Token Cookie名称
     */
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    /**
     * 创建安全的Refresh Token Cookie
     * @param refreshToken Refresh Token值
     * @param maxAge 最大存活时间（秒）
     * @return Cookie对象
     */
    public Cookie createRefreshTokenCookie(String refreshToken, int maxAge) {
        try {
            // URL编码以防止特殊字符问题
            String encodedValue = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
            
            Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, encodedValue);
            cookie.setHttpOnly(true); // 防止JavaScript访问
            cookie.setSecure(secure); // HTTPS环境下启用
            cookie.setMaxAge(maxAge); // 设置过期时间（秒）
            cookie.setPath("/"); // 设置Cookie路径
            
            // 设置域名（如果有配置）
            if (cookieDomain != null && !cookieDomain.isEmpty()) {
                cookie.setDomain(cookieDomain);
            }
            
            return cookie;
        } catch (Exception e) {
            log.error("创建Refresh Token Cookie失败", e);
            throw new RuntimeException("创建Cookie失败");
        }
    }

    /**
     * 从请求中获取Refresh Token
     * @param request HTTP请求
     * @return Refresh Token（如果存在）
     */
    public Optional<String> getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        
        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                try {
                    // 需要对URL编码的值进行解码
                    String encodedValue = cookie.getValue();
                    // 实际上URL编码的Cookie会自动解码，这里直接返回
                    return Optional.of(encodedValue);
                } catch (Exception e) {
                    log.warn("解析Refresh Token Cookie失败", e);
                    return Optional.empty();
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * 清除Refresh Token Cookie
     * @param response HTTP响应
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setMaxAge(0); // 立即过期
        cookie.setPath("/");
        
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }
        
        setSameSiteAttribute(response, cookie);
        log.debug("已清除Refresh Token Cookie");
    }

    /**
     * 设置SameSite属性到响应头（Servlet 4.0兼容方式）
     * @param response HTTP响应
     * @param cookie Cookie对象
     */
    private void setSameSiteAttribute(HttpServletResponse response, Cookie cookie) {
        try {
            // 处理清除cookie的情况（值为null）
            String cookieValue = cookie.getValue() != null ? cookie.getValue() : "";
            
            // 对于Servlet 4.0，通过响应头设置SameSite属性
            StringBuilder cookieHeader = new StringBuilder();
            cookieHeader.append(String.format("%s=%s; Path=%s; HttpOnly; %sSameSite=Strict",
                    cookie.getName(),
                    cookieValue,
                    cookie.getPath(),
                    secure ? "Secure; " : ""));
            
            if (cookie.getMaxAge() > 0) {
                cookieHeader.append(String.format("; Max-Age=%d", cookie.getMaxAge()));
            } else if (cookie.getMaxAge() == 0) {
                cookieHeader.append("; Max-Age=0");
            }
            
            if (cookieDomain != null && !cookieDomain.isEmpty()) {
                cookieHeader.append(String.format("; Domain=%s", cookieDomain));
            }
            
            response.addHeader("Set-Cookie", cookieHeader.toString());
        } catch (Exception e) {
            log.warn("设置SameSite属性失败，将使用标准Cookie设置", e);
            // 回退到标准Cookie设置
            response.addCookie(cookie);
        }
    }

    /**
     * 添加Refresh Token Cookie到响应
     * @param response HTTP响应
     * @param refreshToken Refresh Token值
     * @param maxAge 最大存活时间（秒）
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        Cookie cookie = createRefreshTokenCookie(refreshToken, maxAge);
        setSameSiteAttribute(response, cookie);
        log.debug("已添加Refresh Token Cookie，过期时间: {}秒", maxAge);
    }

    /**
     * 检查请求中是否包含Refresh Token Cookie
     * @param request HTTP请求
     * @return 是否包含有效的Refresh Token Cookie
     */
    public boolean hasRefreshTokenCookie(HttpServletRequest request) {
        return getRefreshTokenFromRequest(request).isPresent();
    }

    /**
     * 安全地从请求中获取Refresh Token，如果不存在则抛出异常
     * @param request HTTP请求
     * @return Refresh Token值
     * @throws IllegalArgumentException 如果Refresh Token不存在
     */
    public String getRefreshTokenRequired(HttpServletRequest request) {
        return getRefreshTokenFromRequest(request)
                .orElseThrow(() -> new IllegalArgumentException("缺少Refresh Token Cookie"));
    }
}