package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付记录表实体类
 */
@Data
@TableName("payment_record")
public class PaymentRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 支付记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 支付方式（0-微信支付，1-支付宝支付）
     */
    private Integer payType;
    
    /**
     * 支付状态（0-待支付，1-支付成功，2-支付失败，3-支付超时）
     */
    private Integer payStatus;
    
    /**
     * 支付流水号
     */
    private String payNo;
    
    /**
     * 第三方支付订单号
     */
    private String thirdPayNo;
    
    /**
     * 支付时间
     */
    private Date payTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}