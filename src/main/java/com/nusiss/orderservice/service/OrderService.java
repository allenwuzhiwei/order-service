package com.nusiss.orderservice.service;

import com.nusiss.orderservice.entity.Order;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/*
 OrderService 接口 - 订单模块业务逻辑接口定义
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

    // 扩展功能
    /*
     扩展功能1：根据用户 ID 查询该用户的所有订单
     @param userId 用户 ID
     @return 用户对应的订单列表
     */
    List<Order> getOrdersByUserId(Long userId);

    /*
     扩展功能2：多条件筛选订单（如状态、时间范围、金额范围）
     所有参数均为可选，可组合使用
     @param status       订单状态（可选）
     @param startDate    下单开始时间（可选）
     @param endDate      下单结束时间（可选）
     @param minAmount    最小金额（可选）
     @param maxAmount    最大金额（可选）
     @return 满足条件的订单列表
     */
    List<Order> filterOrders(String status, Date startDate, Date endDate, Double minAmount, Double maxAmount);

    /*
     扩展功能3：分页获取订单列表，并按指定字段排序
     @param page         当前页码（从 1 开始）
     @param size         每页数量
     @param sortBy       排序字段（如 "order_date", "total_amount"）
     @param sortOrder    排序方式（"asc" 或 "desc"）
     @return 分页后的订单列表
     */
    List<Order> getOrdersWithPaginationAndSorting(int page, int size, String sortBy, String sortOrder);


}
