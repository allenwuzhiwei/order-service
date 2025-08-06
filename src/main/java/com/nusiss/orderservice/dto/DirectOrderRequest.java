package com.nusiss.orderservice.dto;

import lombok.Data;

@Data
public class DirectOrderRequest {
    private Long productId;
    private Integer quantity;
    private String shippingAddress;
    private Long userId;
    private String paymentMethod;
    // 新增字段：用于标识是否启用人脸识别
    private Boolean useFaceRecognition;

    // 新增字段：上传图像的路径（前端或其他服务传入）
    private String faceImagePath;
}
