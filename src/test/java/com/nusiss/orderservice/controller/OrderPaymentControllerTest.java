package com.nusiss.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.nusiss.orderservice.entity.OrderPayment;
import com.nusiss.orderservice.service.OrderPaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderPaymentController.class)
public class OrderPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderPaymentService orderPaymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePayment() throws Exception {
        OrderPayment payment = new OrderPayment();
        Mockito.when(orderPaymentService.createPayment(any())).thenReturn(payment);

        mockMvc.perform(post("/order-payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAllPayments() throws Exception {
        Mockito.when(orderPaymentService.getAllPayments()).thenReturn(List.of(new OrderPayment()));

        mockMvc.perform(get("/order-payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetPaymentById_Found() throws Exception {
        OrderPayment payment = new OrderPayment();
        payment.setPaymentId(1L);
        Mockito.when(orderPaymentService.getPaymentById(1L)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/order-payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").value(1));
    }

    @Test
    void testGetPaymentById_NotFound() throws Exception {
        Mockito.when(orderPaymentService.getPaymentById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/order-payments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdatePayment_Success() throws Exception {
        OrderPayment payment = new OrderPayment();
        Mockito.when(orderPaymentService.updatePayment(any())).thenReturn(true);

        mockMvc.perform(put("/order-payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment updated"));
    }

    @Test
    void testUpdatePayment_NotFound() throws Exception {
        Mockito.when(orderPaymentService.updatePayment(any())).thenReturn(false);

        mockMvc.perform(put("/order-payments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderPayment())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment not found"));
    }

    @Test
    void testDeletePayment_Success() throws Exception {
        Mockito.when(orderPaymentService.deletePayment(1L)).thenReturn(true);

        mockMvc.perform(delete("/order-payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment deleted"));
    }

    @Test
    void testDeletePayment_NotFound() throws Exception {
        Mockito.when(orderPaymentService.deletePayment(999L)).thenReturn(false);

        mockMvc.perform(delete("/order-payments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment not found"));
    }

    @Test
    void testGetPaymentsByOrderId() throws Exception {
        Mockito.when(orderPaymentService.getPaymentsByOrderId(1L))
                .thenReturn(List.of(new OrderPayment()));

        mockMvc.perform(get("/order-payments/by-order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testFilterPayments() throws Exception {
        Mockito.when(orderPaymentService.filterPayments(anyString(), anyString(), any(), any()))
                .thenReturn(List.of(new OrderPayment()));

        mockMvc.perform(get("/order-payments/filter")
                        .param("status", "SUCCESS")
                        .param("method", "WeChat")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCalculateTotalPaid() throws Exception {
        Mockito.when(orderPaymentService.calculateTotalPaidByOrderId(1L)).thenReturn(188.88);

        mockMvc.perform(get("/order-payments/total-paid/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(188.88));
    }
}
