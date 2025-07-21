package com.nusiss.orderservice.dto;

import lombok.Data;

/*
 从购物车创建订单的请求参数
 */
@Data
public class CreateOrderFromCartRequest {
    private Long userId;
    private String shippingAddress;
    private String paymentMethod;
}
