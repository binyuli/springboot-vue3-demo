package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Order;
import com.example.entity.PaymentRecord;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 订单支付
     * @param orderId 订单ID
     * @param payType 支付方式（0-微信支付，1-支付宝支付）
     * @param payNo 支付流水号
     * @return 支付记录
     */
    PaymentRecord payOrder(Long orderId, Integer payType, String payNo);
    
    /**
     * 生成订单
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 商品数量
     * @return 订单信息
     */
    Order createOrder(Long userId, Long productId, Integer quantity);
    
    /**
     * 验证订单状态
     * @param orderId 订单ID
     * @return 订单信息
     */
    Order validateOrderStatus(Long orderId);
    
    /**
     * 扣减库存
     * @param productId 商品ID
     * @param quantity 商品数量
     */
    void deductStock(Long productId, Integer quantity);
    
    /**
     * 生成支付记录
     * @param order 订单信息
     * @param payType 支付方式
     * @param payNo 支付流水号
     * @return 支付记录
     */
    PaymentRecord generatePaymentRecord(Order order, Integer payType, String payNo);
    
    /**
     * 更新订单状态为已支付
     * @param orderId 订单ID
     */
    void updateOrderStatusToPaid(Long orderId);
    
    /**
     * 发送支付成功消息
     * @param order 订单信息
     */
    void sendPaymentSuccessMessage(Order order);
    
    /**
     * 检查支付是否已存在
     * @param payNo 支付流水号
     * @return 是否存在
     */
    boolean checkPaymentExists(String payNo);
}