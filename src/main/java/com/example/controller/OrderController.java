package com.example.controller;

import com.example.entity.Order;
import com.example.entity.PaymentRecord;
import com.example.service.OrderService;
import com.example.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 生成订单
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 商品数量
     * @return 订单信息
     */
    @PostMapping("/create")
    public ResultVO<?> createOrder(@RequestParam Long userId, @RequestParam Long productId, @RequestParam Integer quantity) {
        Order order = orderService.createOrder(userId, productId, quantity);
        return ResultVO.success(order);
    }
    
    /**
     * 订单支付
     * @param orderId 订单ID
     * @param payType 支付方式（0-微信支付，1-支付宝支付）
     * @param payNo 支付流水号
     * @return 支付记录
     */
    @PostMapping("/pay")
    public ResultVO<?> payOrder(@RequestParam Long orderId, @RequestParam Integer payType, @RequestParam String payNo) {
        PaymentRecord paymentRecord = orderService.payOrder(orderId, payType, payNo);
        return ResultVO.success(paymentRecord);
    }
    
    /**
     * 查询订单详情
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/{orderId}")
    public ResultVO<?> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getById(orderId);
        return ResultVO.success(order);
    }
}