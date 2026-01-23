package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param username 用户名（模糊查询）
     * @return 分页结果
     */
    @PostMapping("/page")
    public ResultVO<?> pageUser(@RequestBody UserQueryParams params) {
        Page<User> page = userService.pageUser(params.getPageNum(), params.getPageSize(), params.getUsername());
        
        // 构建返回数据
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        
        return ResultVO.success(result);
    }
    
    /**
     * 查询用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public ResultVO<?> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return ResultVO.success(user);
    }
    
    /**
     * 新增用户
     * @param user 用户信息
     * @return 操作结果
     */
    @PostMapping
    public ResultVO<?> addUser(@RequestBody User user) {
        userService.save(user);
        return ResultVO.success();
    }
    
    /**
     * 更新用户
     * @param user 用户信息
     * @return 操作结果
     */
    @PutMapping
    public ResultVO<?> updateUser(@RequestBody User user) {
        userService.updateById(user);
        return ResultVO.success();
    }
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResultVO<?> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return ResultVO.success();
    }
    
    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public ResultVO<?> batchDeleteUser(@RequestBody List<Long> ids) {
        userService.removeByIds(ids);
        return ResultVO.success();
    }
    
    /**
     * 用户查询参数类
     */
    public static class UserQueryParams {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String username;
        
        // getter和setter方法
        public Integer getPageNum() {
            return pageNum;
        }
        
        public void setPageNum(Integer pageNum) {
            this.pageNum = pageNum;
        }
        
        public Integer getPageSize() {
            return pageSize;
        }
        
        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
    }
}