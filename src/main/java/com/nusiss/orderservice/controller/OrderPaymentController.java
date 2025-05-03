package com.nusiss.orderservice.controller;

import com.nusiss.orderservice.config.ApiResponse;
import com.nusiss.orderservice.entity.OrderPayment;
import com.nusiss.orderservice.service.OrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/*
 OrderPaymentController - 支付模块的 REST 控制器
 处理订单支付相关的 API 请求
 */
@RestController
@RequestMapping("/order-payments")
public class OrderPaymentController {

    @Autowired
    private OrderPaymentService orderPaymentService;

    /*
     创建支付记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderPayment>> createPayment(@RequestBody OrderPayment payment) {
        OrderPayment created = orderPaymentService.createPayment(payment);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "Payment created successfully", created));
    }

    /*
     获取所有支付记录
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderPayment>>> getAllPayments() {
        List<OrderPayment> payments = orderPaymentService.getAllPayments();
        return ResponseEntity.ok(new ApiResponse<>(true, "Payments retrieved successfully", payments));
    }

    /*
     根据支付 ID 获取记录
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<OrderPayment>> getPaymentById(@PathVariable Long paymentId) {
        Optional<OrderPayment> payment = orderPaymentService.getPaymentById(paymentId);
        return payment.map(value -> ResponseEntity.ok(new ApiResponse<>(true, "Payment found", value)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(false, "Payment not found", null)));
    }

    /*
     更新支付记录
     */
    @PutMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Boolean>> updatePayment(@PathVariable Long paymentId,
                                                              @RequestBody OrderPayment updatedPayment) {
        updatedPayment.setPaymentId(paymentId); // 保证 ID 对齐
        boolean updated = orderPaymentService.updatePayment(updatedPayment);
        if (updated) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment updated", true));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "Payment not found", false));
        }
    }

    /*
     删除支付记录
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Boolean>> deletePayment(@PathVariable Long paymentId) {
        boolean deleted = orderPaymentService.deletePayment(paymentId);
        if (deleted) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment deleted", true));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "Payment not found", false));
        }
    }

    /*
    扩展功能1: 根据订单 ID 获取该订单的所有支付记录
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderPayment>>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<OrderPayment> payments = orderPaymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payments by order retrieved", payments));
    }

    /*
     扩展功能2: 多条件筛选支付记录
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<OrderPayment>>> filterPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<OrderPayment> results = orderPaymentService.filterPayments(status, method, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Filtered payments retrieved", results));
    }

    /*
     扩展功能3: 获取某订单的累计支付金额
     */
    @GetMapping("/total-paid/{orderId}")
    public ResponseEntity<ApiResponse<Double>> calculateTotalPaid(@PathVariable Long orderId) {
        Double total = orderPaymentService.calculateTotalPaidByOrderId(orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Total paid amount calculated", total));
    }
}
