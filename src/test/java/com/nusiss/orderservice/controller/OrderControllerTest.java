package com.nusiss.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nusiss.commonservice.config.ApiResponse;
import com.nusiss.orderservice.dto.CreateOrderFromCartRequest;
import com.nusiss.orderservice.dto.DirectOrderRequest;
import com.nusiss.orderservice.entity.Order;
import com.nusiss.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;


@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateDirectOrder() throws Exception {
        Order order = new Order();
        DirectOrderRequest request = new DirectOrderRequest();
        Mockito.when(orderService.createDirectOrder(any())).thenReturn(order);

        mockMvc.perform(post("/orders/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCreateOrderFromCart() throws Exception {
        Order order = new Order();
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        Mockito.when(orderService.createOrderFromCart(any())).thenReturn(order);

        mockMvc.perform(post("/orders/fromCart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAllOrders() throws Exception {
        Mockito.when(orderService.getAllOrders()).thenReturn(List.of(new Order()));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetOrderById_Found() throws Exception {
        Order order = new Order();
        order.setOrderId(1L);
        Mockito.when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(1));
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        Mockito.when(orderService.getOrderById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdateOrder_Success() throws Exception {
        Order order = new Order();
        order.setOrderId(1L);
        Mockito.when(orderService.updateOrder(any())).thenReturn(true);

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateOrder_NotFound() throws Exception {
        Mockito.when(orderService.updateOrder(any())).thenReturn(false);
        Order order = new Order();
        order.setOrderId(99L);

        mockMvc.perform(put("/orders/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testDeleteOrder_Success() throws Exception {
        Mockito.when(orderService.deleteOrder(1L)).thenReturn(true);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteOrder_NotFound() throws Exception {
        Mockito.when(orderService.deleteOrder(99L)).thenReturn(false);

        mockMvc.perform(delete("/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetOrdersByUserId() throws Exception {
        Mockito.when(orderService.getOrdersByUserId(10L)).thenReturn(List.of(new Order()));

        mockMvc.perform(get("/orders/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testFilterOrders() throws Exception {
        Mockito.when(orderService.filterOrders(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new Order()));

        mockMvc.perform(get("/orders/filter")
                        .param("status", "SHIPPED")
                        .param("minAmount", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetOrdersWithPaginationAndSorting() throws Exception {
        Mockito.when(orderService.getOrdersWithPaginationAndSorting(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(List.of(new Order()));

        mockMvc.perform(get("/orders/paged")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "orderDate")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
