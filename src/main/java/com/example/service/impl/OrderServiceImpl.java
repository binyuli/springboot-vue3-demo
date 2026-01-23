package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Order;
import com.example.entity.PaymentRecord;
import com.example.exception.BusinessException;
import com.example.mapper.OrderMapper;
import com.example.mapper.PaymentRecordMapper;
import com.example.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    
    /**
     * 订单支付，实现分布式事务（使用本地事务表）
     * 支付流程：验证订单状态→扣减库存→生成支付记录→更新订单状态→发送支付成功消息
     * 
     * @param orderId 订单ID
     * @param payType 支付方式（0-微信支付，1-支付宝支付）
     * @param payNo 支付流水号
     * @return 支付记录
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public PaymentRecord payOrder(Long orderId, Integer payType, String payNo) {
        // 1. 接口幂等性校验：基于支付流水号
        if (checkPaymentExists(payNo)) {
            log.warn("支付已存在，流水号：{}", payNo);
            throw new BusinessException(400, "该支付已处理");
        }
        
        // 2. 验证订单状态
        Order order = validateOrderStatus(orderId);
        
        // 3. 扣减库存
        try {
            deductStock(order.getProductId(), order.getQuantity());
        } catch (Exception e) {
            log.error("扣减库存失败，商品ID：{}，数量：{}", order.getProductId(), order.getQuantity(), e);
            throw new BusinessException(500, "库存不足");
        }
        
        // 4. 生成支付记录
        PaymentRecord paymentRecord = generatePaymentRecord(order, payType, payNo);
        boolean saveResult = paymentRecordMapper.insert(paymentRecord) > 0;
        if (!saveResult) {
            log.error("生成支付记录失败，订单ID：{}", orderId);
            throw new BusinessException(500, "生成支付记录失败");
        }
        
        // 5. 更新订单状态为已支付
        updateOrderStatusToPaid(orderId);
        
        // 6. 发送支付成功消息
        sendPaymentSuccessMessage(order);
        
        log.info("订单支付成功，订单ID：{}，支付流水号：{}", orderId, payNo);
        return paymentRecord;
    }
    
    /**
     * 生成订单
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 商品数量
     * @return 订单信息
     */
    @Override
    public Order createOrder(Long userId, Long productId, Integer quantity) {
        // 模拟创建订单，实际项目中需要从商品服务获取商品信息和库存
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName("测试商品");
        order.setQuantity(quantity);
        order.setPrice(new BigDecimal(100));
        order.setTotalAmount(new BigDecimal(100).multiply(new BigDecimal(quantity)));
        order.setStatus(0); // 0-待支付
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        
        boolean saveResult = save(order);
        if (!saveResult) {
            throw new BusinessException(500, "生成订单失败");
        }
        
        return order;
    }
    
    /**
     * 验证订单状态
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    @Override
    public Order validateOrderStatus(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            String statusMsg = switch (order.getStatus()) {
                case 1 -> "订单已支付";
                case 2 -> "订单已取消";
                case 3 -> "订单已超时";
                default -> "订单状态异常";
            };
            throw new BusinessException(400, statusMsg);
        }
        
        // 检查订单是否超时（这里简单模拟，实际项目中需要根据创建时间判断）
        long currentTime = System.currentTimeMillis();
        long orderCreateTime = order.getCreateTime().getTime();
        long timeout = 30 * 60 * 1000; // 30分钟超时
        if (currentTime - orderCreateTime > timeout) {
            // 更新订单状态为已超时
            order.setStatus(3);
            order.setUpdateTime(new Date());
            updateById(order);
            throw new BusinessException(400, "订单已超时");
        }
        
        return order;
    }
    
    /**
     * 扣减库存
     * 
     * @param productId 商品ID
     * @param quantity 商品数量
     */
    @Override
    public void deductStock(Long productId, Integer quantity) {
        // 模拟扣减库存，实际项目中需要调用库存服务
        log.info("扣减库存，商品ID：{}，数量：{}", productId, quantity);
        
        // 模拟库存不足的情况
        if (Math.random() < 0.1) { // 10%概率模拟库存不足
            throw new BusinessException(400, "库存不足");
        }
    }
    
    /**
     * 生成支付记录
     * 
     * @param order 订单信息
     * @param payType 支付方式
     * @param payNo 支付流水号
     * @return 支付记录
     */
    @Override
    public PaymentRecord generatePaymentRecord(Order order, Integer payType, String payNo) {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(order.getId());
        paymentRecord.setOrderNo(order.getOrderNo());
        paymentRecord.setUserId(order.getUserId());
        paymentRecord.setPayAmount(order.getTotalAmount());
        paymentRecord.setPayType(payType);
        paymentRecord.setPayStatus(1); // 1-支付成功
        paymentRecord.setPayNo(payNo);
        paymentRecord.setThirdPayNo(generateThirdPayNo()); // 模拟第三方支付订单号
        paymentRecord.setPayTime(new Date());
        paymentRecord.setCreateTime(new Date());
        paymentRecord.setUpdateTime(new Date());
        
        return paymentRecord;
    }
    
    /**
     * 更新订单状态为已支付
     * 
     * @param orderId 订单ID
     */
    @Override
    public void updateOrderStatusToPaid(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(1); // 1-已支付
        order.setPayTime(new Date());
        order.setUpdateTime(new Date());
        
        boolean updateResult = updateById(order);
        if (!updateResult) {
            log.error("更新订单状态失败，订单ID：{}", orderId);
            throw new BusinessException(500, "更新订单状态失败");
        }
    }
    
    /**
     * 发送支付成功消息
     * 
     * @param order 订单信息
     */
    @Override
    public void sendPaymentSuccessMessage(Order order) {
        // 模拟发送支付成功消息，实际项目中需要使用消息队列
        log.info("发送支付成功消息，订单ID：{}，订单号：{}", order.getId(), order.getOrderNo());
    }
    
    /**
     * 检查支付是否已存在
     * 
     * @param payNo 支付流水号
     * @return 是否存在
     */
    @Override
    public boolean checkPaymentExists(String payNo) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getPayNo, payNo);
        return paymentRecordMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 生成订单号
     * 
     * @return 订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 生成第三方支付订单号
     * 
     * @return 第三方支付订单号
     */
    private String generateThirdPayNo() {
        return "TP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}