package com.nusiss.orderservice.controller;

import com.nusiss.orderservice.config.ApiResponse;
import com.nusiss.orderservice.entity.OrderShipment;
import com.nusiss.orderservice.service.OrderShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/*
 OrderShipmentController - 发货模块的 REST 控制器
 负责处理与订单发货相关的所有 HTTP 请求
 包括：创建、更新、删除、按订单ID查询、查询全部
 */
@RestController
@RequestMapping("/order-shipments")
public class OrderShipmentController {

    @Autowired
    private OrderShipmentService shipmentService;

    /*
     创建发货记录
     会根据发货状态调用对应的策略进行发货时间处理
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderShipment>> createShipment(@RequestBody OrderShipment shipment) {
        OrderShipment created = shipmentService.createShipment(shipment);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "Shipment created", created));
    }

    /*
     更新发货记录（例如更新发货状态、时间等）
     会自动触发策略模式中的状态驱动逻辑
     */
    @PutMapping("/{shipmentId}")
    public ResponseEntity<ApiResponse<Boolean>> updateShipment(@PathVariable Long shipmentId,
                                                               @RequestBody OrderShipment updatedShipment) {
        boolean updated = shipmentService.updateShipment(shipmentId, updatedShipment);
        if (updated) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Shipment updated", true));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "Shipment not found", false));
        }
    }

    /*
     删除发货记录
     */
    @DeleteMapping("/{shipmentId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteShipment(@PathVariable Long shipmentId) {
        boolean deleted = shipmentService.deleteShipment(shipmentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Shipment deleted", deleted));
    }

    /*
     根据订单ID获取发货记录
     （由于业务设定为一单一发货，所以结果为 Optional<OrderShipment>）
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<ApiResponse<OrderShipment>> getShipmentByOrderId(@PathVariable Long orderId) {
        Optional<OrderShipment> result = shipmentService.getShipmentByOrderId(orderId);
        return result.map(shipment -> ResponseEntity.ok(new ApiResponse<>(true, "Shipment found", shipment)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(false, "Shipment not found", null)));
    }

    /*
     获取全部发货记录
     主要用于测试或管理端使用
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderShipment>>> getAllShipments() {
        List<OrderShipment> shipments = shipmentService.getAllShipments();
        return ResponseEntity.ok(new ApiResponse<>(true, "All shipments retrieved", shipments));
    }
}
