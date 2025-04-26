package com.nusiss.orderservice.service.impl;

import com.nusiss.orderservice.entity.Order;
import com.nusiss.orderservice.dao.OrderRepository;
import com.nusiss.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * OrderServiceImpl 实现类 - 提供订单模块的业务逻辑实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    /*
     创建订单
     */
    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    /*
     根据订单 ID 查询
     */
    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /*
     查询所有订单
     */
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /*
     更新订单信息（如果存在该订单则更新）
     */
    @Override
    public boolean updateOrder(Order order) {
        if (orderRepository.existsById(order.getOrderId())) {
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    /*
     删除订单
     */
    @Override
    public boolean deleteOrder(Long orderId) {
        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
            return true;
        }
        return false;
    }


    /*
     扩展功能1：根据用户 ID 查询该用户的所有订单
     */
    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

}
