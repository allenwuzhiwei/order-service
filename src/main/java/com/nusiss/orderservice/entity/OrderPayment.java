package com.nusiss.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/*
 OrderPayment 实体类，对应数据库中的 order_payments 表
 用于记录每笔订单的支付信息
 */
@Data
@Entity
@Table(name = "order_payments")
public class OrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long paymentId; // 支付记录主键

    private Long orderId; // 对应的订单ID（外键）

    private String paymentMethod; // 支付方式，例如：Credit Card、PayPal 等

    private String paymentStatus; // 支付状态，例如：PAID、FAILED、PENDING

    private String transactionRef; // 第三方交易流水号（如支付宝/Stripe 返回）

    private LocalDateTime paymentDate; // 支付时间

    private Double amountPaid; // 实际支付金额

    private String createUser; // 创建人
    private String updateUser; // 更新人
    private LocalDateTime createDatetime; // 创建时间
    private LocalDateTime updateDatetime; // 更新时间
}
