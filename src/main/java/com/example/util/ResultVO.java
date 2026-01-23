package com.example.util;

import lombok.Data;

/**
 * 统一返回结果封装类
 */
@Data
public class ResultVO<T> {
    /**
     * 响应码，200表示成功
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 私有构造方法，防止外部直接创建
     */
    private ResultVO() {
    }
    
    /**
     * 私有构造方法，带参数
     */
    private ResultVO(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    /**
     * 成功返回，带数据
     */
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "success", data);
    }
    
    /**
     * 成功返回，不带数据
     */
    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "success", null);
    }
    
    /**
     * 失败返回，带错误码和错误消息
     */
    public static <T> ResultVO<T> error(int code, String msg) {
        return new ResultVO<>(code, msg, null);
    }
    
    /**
     * 失败返回，带错误消息
     */
    public static <T> ResultVO<T> error(String msg) {
        return new ResultVO<>(500, msg, null);
    }
}