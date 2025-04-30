package com.nusiss.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/*
 OrderItem 实体类 - 对应数据库中的 order_items 表
 用于表示订单中的单个商品项信息
 */
@Data
@Entity
@Table(name = "order_items")  // 注意：表名使用小写，防止大小写兼容问题
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long itemId; // 订单项 ID（主键）

    private Long orderId; // 所属订单 ID

    private Long productId; // 商品 ID

    private String productName; // 商品名称

    private Double productPrice; // 商品单价

    private Integer quantity; // 商品数量

    @Column(insertable = false, updatable = false)
    private Double subtotalAmount; // 小计金额 = 单价 × 数量（可由后端自动计算）

    private String createUser; // 创建人
    private String updateUser; // 更新人

    private LocalDateTime createDatetime; // 创建时间
    private LocalDateTime updateDatetime; // 更新时间
}
