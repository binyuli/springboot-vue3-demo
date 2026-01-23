package com.example.exception;

import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;
    
    /**
     * 构造方法
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造方法，使用默认错误码
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}