package com.nusiss.orderservice.controller;

import com.nusiss.orderservice.config.ApiResponse;
import com.nusiss.orderservice.entity.Order;
import com.nusiss.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/*
 OrderController - 订单模块的 REST 控制器
 提供订单的基本增删改查以及扩展功能接口
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /*
     创建订单
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        Order created = orderService.createOrder(order);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "订单创建成功", created));
    }

    /*
     获取所有订单
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(new ApiResponse<>(true, "获取全部订单成功", orders));
    }

    /*
     根据订单ID获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        return order.map(value -> ResponseEntity.ok(new ApiResponse<>(true, "获取订单成功", value)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(false, "订单不存在", null)));
    }

    /*
     更新订单
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<String>> updateOrder(@PathVariable Long orderId, @RequestBody Order updatedOrder) {
        updatedOrder.setOrderId(orderId); // 确保更新对象 ID 正确
        boolean success = orderService.updateOrder(updatedOrder);
        if (success) {
            return ResponseEntity.ok(new ApiResponse<>(true, "订单更新成功", null));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "订单不存在", null));
        }
    }

    /*
     删除订单
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<String>> deleteOrder(@PathVariable Long orderId) {
        boolean deleted = orderService.deleteOrder(orderId);
        if (deleted) {
            return ResponseEntity.ok(new ApiResponse<>(true, "订单删除成功", null));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "订单不存在", null));
        }
    }



    /*
     扩展功能1：根据用户ID查询该用户所有订单
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "获取用户订单成功", orders));
    }

    /*
     扩展功能2：多条件筛选订单
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<Order>>> filterOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        List<Order> filtered = orderService.filterOrders(status, startDate, endDate, minAmount, maxAmount);
        return ResponseEntity.ok(new ApiResponse<>(true, "筛选订单成功", filtered));
    }

    /*
     扩展功能3：分页+排序
     */
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersWithPaginationAndSorting(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        List<Order> orders = orderService.getOrdersWithPaginationAndSorting(page, size, sortBy, sortOrder);
        return ResponseEntity.ok(new ApiResponse<>(true, "分页获取订单成功", orders));
    }

}
