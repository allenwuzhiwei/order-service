package com.nusiss.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.nusiss.orderservice.dto.CreateOrderFromCartRequest;
import com.nusiss.orderservice.dto.DirectOrderRequest;

import com.nusiss.orderservice.entity.Order;
import com.nusiss.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private ObjectMapper objectMapper;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockOrder = new Order();
        mockOrder.setOrderId(1L);
        mockOrder.setShippingAddress("Test Address");
        mockOrder.setPaymentStatus("PAID");
        mockOrder.setTotalAmount(BigDecimal.valueOf(100));
        mockOrder.setOrderStatus("CREATED");
        mockOrder.setCreateDatetime(LocalDateTime.now());
    }

    @Test
    void testCreateDirectOrder() throws Exception {
        DirectOrderRequest request = new DirectOrderRequest();
        request.setProductId(1L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");
        request.setUserId(100L);
        request.setPaymentMethod("WeChat");

        Mockito.when(orderService.createDirectOrder(any())).thenReturn(mockOrder);

        mockMvc.perform(post("/orders/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderId", is(1)));
    }

    @Test
    void testCreateOrderWithFaceRecognition() throws Exception {
        MockMultipartFile faceImage = new MockMultipartFile("faceImage", "face.jpg", "image/jpeg", new byte[]{1, 2, 3});

        Mockito.when(orderService.createOrderWithFaceRecognition(any(), any())).thenReturn(mockOrder);

        mockMvc.perform(multipart("/orders/direct/face-recognition")
                        .file(faceImage)
                        .param("productId", "1")
                        .param("quantity", "2")
                        .param("shippingAddress", "Test Address")
                        .param("paymentMethod", "WeChat")
                        .param("useFaceRecognition", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderId", is(1)));
    }

    @Test
    void testCreateOrderFromCart() throws Exception {
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(100L);
        request.setShippingAddress("Test Address");
        request.setPaymentMethod("WeChat");

        Mockito.when(orderService.createOrderFromCart(any())).thenReturn(mockOrder);

        mockMvc.perform(post("/orders/fromCart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderId", is(1)));
    }

    @Test
    void testCreateOrderFromCartWithFaceRecognition() throws Exception {
        MockMultipartFile faceImage = new MockMultipartFile("faceImage", "face.jpg", "image/jpeg", new byte[]{1, 2, 3});

        Mockito.when(orderService.createOrderFromCartWithFaceRecognition(any(), any())).thenReturn(mockOrder);

        mockMvc.perform(multipart("/orders/fromCart/face-recognition")
                        .file(faceImage)
                        .param("shippingAddress", "Test Address")
                        .param("paymentMethod", "WeChat")
                        .param("useFaceRecognition", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderId", is(1)));
    }

    @Test
    void testGetAllOrders() throws Exception {
        List<Order> orders = List.of(mockOrder, mockOrder);
        Mockito.when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void testGetOrderById_found() throws Exception {
        Mockito.when(orderService.getOrderById(1L)).thenReturn(Optional.of(mockOrder));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderId", is(1)));
    }

    @Test
    void testGetOrderById_notFound() throws Exception {
        Mockito.when(orderService.getOrderById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("订单不存在")));
    }

    @Test
    void testUpdateOrder_success() throws Exception {
        Order updatedOrder = new Order();
        updatedOrder.setShippingAddress("Updated");
        updatedOrder.setOrderStatus("SHIPPED");

        Mockito.when(orderService.updateOrder(any(Order.class))).thenReturn(true);

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("订单更新成功")));
    }

    @Test
    void testUpdateOrder_notFound() throws Exception {
        Order updatedOrder = new Order();
        updatedOrder.setShippingAddress("Updated");

        Mockito.when(orderService.updateOrder(any(Order.class))).thenReturn(false);

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOrder)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("订单不存在")));
    }


    @Test
    void testDeleteOrder_success() throws Exception {
        Mockito.when(orderService.deleteOrder(1L)).thenReturn(true);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("订单删除成功")));
    }

    @Test
    void testDeleteOrder_notFound() throws Exception {
        Mockito.when(orderService.deleteOrder(2L)).thenReturn(false);

        mockMvc.perform(delete("/orders/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("订单不存在")));
    }


}
