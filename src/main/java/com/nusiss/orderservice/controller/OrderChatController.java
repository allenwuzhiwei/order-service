package com.nusiss.orderservice.controller;

import com.nusiss.orderservice.dao.OrderRepository;
import com.nusiss.orderservice.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/*
 ChatBox 交互相关接口：用于返回简洁的文本内容供 ChatBox 展示
 */
@RestController
@RequestMapping("/api/chat/order")
public class OrderChatController {

    @Autowired
    private OrderRepository orderRepository;

    // 接口1：查询最近订单状态
    @GetMapping("/latest-status")
    public Map<String, Object> getLatestOrderStatus(@RequestParam Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreateDatetimeDesc(userId);
        if (orders.isEmpty()) {
            return responseText("您还没有下过订单哦～");
        }
        Order latestOrder = orders.get(0);
        return responseText("您最近的订单状态是：" + latestOrder.getOrderStatus());
    }

    // 接口2：查询未支付订单数量
    @GetMapping("/unpaid-count")
    public Map<String, Object> getUnpaidOrderCount(@RequestParam Long userId) {
        int count = orderRepository.countByUserIdAndOrderStatus(userId, "UNPAID");
        return responseText("您还有 " + count + " 个订单未完成支付。");
    }

    // 接口3：查询订单总数与完成数
    @GetMapping("/statistics")
    public Map<String, Object> getOrderStatistics(@RequestParam Long userId) {
        int total = orderRepository.countByUserId(userId);
        int completed = orderRepository.countByUserIdAndOrderStatus(userId, "COMPLETED");
        return responseText("您共下单 " + total + " 次，其中已完成 " + completed + " 个订单。");
    }

    // 统一返回格式（ChatBox 文本块格式）
    private Map<String, Object> responseText(String text) {
        return Map.of("parts", List.of(
                Map.of("type", "text", "text", text)
        ));
    }
}
