package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.User;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);
    
    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param username 用户名（模糊查询）
     * @return 分页结果
     */
    Page<User> pageUser(Integer pageNum, Integer pageSize, String username);
}