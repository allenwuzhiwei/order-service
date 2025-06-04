package com.nusiss.orderservice.controller;

import com.nusiss.commonservice.config.ApiResponse;
import com.nusiss.orderservice.entity.OrderItem;
import com.nusiss.orderservice.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/*
 OrderItemController - 订单项模块的 REST 控制器
 提供订单项的增删改查及扩展操作
 */
@RestController
@RequestMapping("/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    /*创建订单项,已弃用*/
//    @PostMapping
//    public ResponseEntity<ApiResponse<OrderItem>> createItem(@RequestBody OrderItem item) {
//        OrderItem created = orderItemService.addOrderItem(item);
//        return ResponseEntity.ok(new ApiResponse<>(true, "订单项创建成功", created));
//    }


    // 查询某订单下的所有订单项
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderItem>>> getItemsByOrderId(@PathVariable Long orderId) {
        List<OrderItem> items = orderItemService.getItemsByOrderId(orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "查询成功", items));
    }

    // 更新订单项
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Boolean>> updateItem(@PathVariable Long itemId, @RequestBody OrderItem item) {
        boolean success = orderItemService.updateOrderItem(itemId, item);
        return ResponseEntity.ok(new ApiResponse<>(success, success ? "更新成功" : "更新失败", success));
    }

    // 删除订单项
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteItem(@PathVariable Long itemId) {
        boolean success = orderItemService.deleteOrderItem(itemId);
        return ResponseEntity.ok(new ApiResponse<>(success, success ? "删除成功" : "删除失败", success));
    }

    // 扩展功能 1：获取订单的总金额
    @GetMapping("/total/{orderId}")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateTotal(@PathVariable Long orderId) {
        BigDecimal total = orderItemService.calculateTotalAmountByOrderId(orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "计算成功", total));
    }

    // 扩展功能 2：批量添加订单项
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<OrderItem>>> addBatch(@RequestBody List<OrderItem> items) {
        List<OrderItem> saved = orderItemService.addOrderItemsInBatch(items);
        return ResponseEntity.ok(new ApiResponse<>(true, "批量添加成功", saved));
    }

    // 扩展功能 3：批量删除订单项
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Boolean>> deleteBatch(@RequestBody List<Long> itemIds) {
        boolean success = orderItemService.deleteOrderItemsInBatch(itemIds);
        return ResponseEntity.ok(new ApiResponse<>(success, success ? "批量删除成功" : "批量删除失败", success));
    }

    // 工具类功能 4：根据用户ID获取产品ID列表
    @GetMapping("/user/{userId}/product-ids")
    public List<Long> getProductIdsByUserId(@PathVariable Long userId) {
        return orderItemService.getProductIdsByUserId(userId);
    }


}
