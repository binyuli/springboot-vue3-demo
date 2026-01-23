package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 用户Service实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Redis缓存前缀
     */
    private static final String USER_CACHE_PREFIX = "user:";
    
    /**
     * 缓存过期时间（30分钟）
     */
    private static final long CACHE_EXPIRE_TIME = 30;
    
    @Override
    public User findByUsername(String username) {
        // 构建缓存key
        String cacheKey = USER_CACHE_PREFIX + "username:" + username;
        
        // 从缓存中获取用户信息
        User user = (User) redisTemplate.opsForValue().get(cacheKey);
        
        if (user == null) {
            // 缓存不存在，从数据库查询
            user = baseMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username)
                    .eq(User::getDeleted, 0));
            
            if (user != null) {
                // 将用户信息存入缓存，设置30分钟过期
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
                log.info("用户信息已存入缓存: {}", username);
            }
        } else {
            log.info("从缓存中获取用户信息: {}", username);
        }
        
        return user;
    }
    
    @Override
    public Page<User> pageUser(Integer pageNum, Integer pageSize, String username) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0);
        
        // 用户名模糊查询
        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(User::getCreateTime);
        
        // 执行分页查询
        Page<User> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 重写save方法，添加缓存逻辑
     */
    @Override
    public boolean save(User user) {
        boolean result = super.save(user);
        if (result) {
            // 清除相关缓存
            clearUserCache(user.getUsername());
        }
        return result;
    }
    
    /**
     * 重写updateById方法，添加缓存逻辑
     */
    @Override
    public boolean updateById(User user) {
        // 获取原用户名
        User oldUser = baseMapper.selectById(user.getId());
        boolean result = super.updateById(user);
        
        if (result) {
            // 清除原用户名缓存
            clearUserCache(oldUser.getUsername());
            // 清除新用户名缓存
            clearUserCache(user.getUsername());
        }
        return result;
    }
    
    /**
     * 重写removeById方法，添加缓存逻辑
     */
    @Override
    public boolean removeById(Serializable id) {
        // 获取原用户名
        User user = baseMapper.selectById(id);
        boolean result = super.removeById(id);
        
        if (result) {
            // 清除缓存
            clearUserCache(user.getUsername());
        }
        return result;
    }
    
    /**
     * 清除用户缓存
     */
    private void clearUserCache(String username) {
        String cacheKey = USER_CACHE_PREFIX + "username:" + username;
        redisTemplate.delete(cacheKey);
        log.info("用户缓存已清除: {}", username);
    }
}