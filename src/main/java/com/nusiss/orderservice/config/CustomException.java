package com.nusiss.orderservice.config;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}