package com.nusiss.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/*
 OrderShipment 实体类 - 映射 order_shipments 表
 */
@Data
@Entity
@Table(name = "order_shipments")
public class OrderShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentId;

    private Long orderId;
    private String trackingNumber;
    private String carrier;
    private String shipmentStatus;

    private LocalDateTime shippedDate;
    private LocalDateTime deliveryDate;

    private String createUser;
    private String updateUser;
    private LocalDateTime createDatetime;
    private LocalDateTime updateDatetime;
}
