package com.nusiss.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 Order 实体类，对应数据库中的 Orders 表
 用于存储每一笔订单的核心信息
 */
@Data // Lombok 自动生成 Getter、Setter、toString 等方法
@Entity // 表示这是一个 JPA 实体类
@Table(name = "orders") // 指定数据库中的表名
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long orderId; // 订单ID（主键）

    private Long userId; // 用户ID，表示订单属于哪个用户

    private String orderStatus; // 订单状态，例如：PENDING、PAID、SHIPPED 等

    private BigDecimal totalAmount; // 订单总金额

    private String paymentStatus; // 支付状态，例如：UNPAID、PAID

    private String shippingAddress; // 收货地址

    private LocalDateTime orderDate; // 下单时间

    private LocalDateTime deliveryDate; // 预期送达时间

    private String createUser; // 创建人
    private String updateUser; // 更新人
    private LocalDateTime createDatetime; // 创建时间
    private LocalDateTime updateDatetime; // 更新时间
}
