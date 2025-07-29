package com.nusiss.orderservice.controller;

import com.nusiss.orderservice.dao.OrderRepository;
import com.nusiss.orderservice.entity.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(OrderChatController.class)
public class OrderChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @Test
    void testGetLatestOrderStatus_hasOrder() throws Exception {
        Order order = new Order();
        order.setOrderStatus("COMPLETED");
        order.setCreateDatetime(LocalDateTime.now());

        Mockito.when(orderRepository.findByUserIdOrderByCreateDatetimeDesc(1L))
                .thenReturn(List.of(order));

        mockMvc.perform(get("/api/chat/order/latest-status").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parts[0].text").value(containsString("您最近的订单状态是：COMPLETED")));
    }

    @Test
    void testGetLatestOrderStatus_noOrder() throws Exception {
        Mockito.when(orderRepository.findByUserIdOrderByCreateDatetimeDesc(2L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/chat/order/latest-status").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parts[0].text").value("您还没有下过订单哦～"));
    }

    @Test
    void testGetUnpaidOrderCount() throws Exception {
        Mockito.when(orderRepository.countByUserIdAndOrderStatus(1L, "UNPAID"))
                .thenReturn(3);

        mockMvc.perform(get("/api/chat/order/unpaid-count").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parts[0].text").value(containsString("您还有 3 个订单未完成支付。")));
    }

    @Test
    void testGetOrderStatistics() throws Exception {
        Mockito.when(orderRepository.countByUserId(1L)).thenReturn(10);
        Mockito.when(orderRepository.countByUserIdAndOrderStatus(1L, "COMPLETED")).thenReturn(6);

        mockMvc.perform(get("/api/chat/order/statistics").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parts[0].text").value(containsString("您共下单 10 次，其中已完成 6 个订单")));
    }
}
