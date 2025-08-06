package com.nusiss.orderservice.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/*
 从购物车创建订单的请求参数
 */
@Data
public class CreateOrderFromCartRequest {
    private Long userId;
    private String shippingAddress;
    private String paymentMethod;
    // 新增字段：用于标识是否启用人脸识别
    private Boolean useFaceRecognition;

    // 新增字段：上传图像的路径（前端或其他服务传入）
    private String faceImagePath;
    private MultipartFile faceImage;
}
