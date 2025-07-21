package com.nusiss.orderservice.dto;

import lombok.Data;

@Data
public class DirectOrderRequest {
    private Long productId;
    private Integer quantity;
    private String shippingAddress;
    private Long userId;
    private String paymentMethod;
}
