package com.nusiss.orderservice.service;

import com.nusiss.orderservice.entity.Order;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * OrderService 接口 - 订单模块业务逻辑接口定义
 */
public interface OrderService {

    //基础功能
    /*
     创建订单
     @param order 订单对象
     @return 创建成功后的订单
     */
    Order createOrder(Order order);

    /*
     根据 ID 查询订单详情
     @param orderId 订单主键
     @return 对应的订单对象，若不存在返回 Optional.empty()
     */
    Optional<Order> getOrderById(Long orderId);

    /*
     查询所有订单列表
     @return 所有订单
     */
    List<Order> getAllOrders();

    /*
     更新订单信息
     @param order 更新后的订单对象
     @return 更新是否成功
     */
    boolean updateOrder(Order order);

    /*
     删除订单
     @param orderId 要删除的订单主键 ID
     @return 是否删除成功
     */
    boolean deleteOrder(Long orderId);


}
