package com.nusiss.orderservice.dto;

import lombok.Data;

@Data
public class FacePaymentDirectOrderRequest extends DirectOrderRequest {
    private Boolean useFaceRecognition;     // 是否启用人脸识别
    private String faceImagePath;           // 人脸图片路径或 Base64 字符串
}
