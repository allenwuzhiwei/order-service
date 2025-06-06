package com.nusiss.orderservice.controller;

import com.nusiss.commonservice.config.ApiResponse;
import com.nusiss.orderservice.dto.CreateOrderFromCartRequest;
import com.nusiss.orderservice.dto.DirectOrderRequest;
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
     已废用--创建订单
     */
//    @PostMapping
//    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
//        Order created = orderService.createOrder(order);
//        return ResponseEntity.status(201).body(new ApiResponse<>(true, "订单创建成功", created));
//    }

    /*
    通过Feign进行联动接口：用于前端调用【创建订单 + 获取商品详情 + 验证库存 + 选择支付方式（支付成功后）（预留） + 扣减商品库存】这一整套流程。
    */
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<Order>> createDirectOrder(@RequestBody DirectOrderRequest request) {
        Order createdOrder = orderService.createDirectOrder(request);
        return ResponseEntity.ok(ApiResponse.success(createdOrder));
    }

    /*
     从购物车创建订单（校验库存 + 创建订单 + 扣库存 + 清空购物车）
     @param request 请求体，包括 userId 和 shippingAddress
     @return 创建好的订单信息
     */
    @PostMapping("/fromCart")
    public ResponseEntity<ApiResponse<Order>> createOrderFromCart(@RequestBody CreateOrderFromCartRequest request) {
        Order createdOrder = orderService.createOrderFromCart(request);
        return ResponseEntity.ok(ApiResponse.success(createdOrder));
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
