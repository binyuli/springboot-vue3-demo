package com.example.config;

import com.example.service.UserService;
import com.example.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器，用于验证请求中的token
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    private UserDetails loadUserByUsername(String username) {
        // 1. 从数据库/缓存加载用户信息
        com.example.entity.User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 2. 检查用户状态
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            throw new UsernameNotFoundException("用户已被删除");
        }
        
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用");
        }

        // 3. 根据用户状态、角色构建权限列表
        List<GrantedAuthority> authorities = new ArrayList<>();

        // user table doesn't have role field, so we just add ROLE_USER
        // if ("admin".equals(user.getRole())) {
        //     authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // }
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(username, "", authorities);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // 对于OPTIONS请求（CORS预检），完全跳过JWT验证
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        
        // 对于login、refresh和logout路径，完全跳过JWT验证
        return path.contains("/auth/login") || path.contains("/auth/refresh") || path.contains("/auth/logout");
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 清理之前的认证信息 - 防御性编程
        SecurityContextHolder.clearContext();
        
        try {
            // 从请求头中获取token
            String token = getTokenFromRequest(request);
            
            if (StringUtils.hasText(token)) {
                // 解析token
                Claims claims = jwtUtil.parseToken(token);
                String username = claims.getSubject();
                
                // 创建用户详情对象
                UserDetails userDetails = loadUserByUsername(username);

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // 设置认证信息到上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            
            // 设置401响应
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 401, \"message\": \"认证失败\"}");
            return; // 不再继续执行过滤链
        }
        
        // 继续执行过滤链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求头中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}