package com.nusiss.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.nusiss.orderservice.entity.OrderItem;
import com.nusiss.orderservice.service.OrderItemService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderItemController.class)
public class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderItemService orderItemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetItemsByOrderId() throws Exception {
        when(orderItemService.getItemsByOrderId(1L))
                .thenReturn(List.of(new OrderItem()));

        mockMvc.perform(get("/order-items/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateItem_Success() throws Exception {
        OrderItem item = new OrderItem();
        when(orderItemService.updateOrderItem(eq(1L), any(OrderItem.class)))
                .thenReturn(true);

        mockMvc.perform(put("/order-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("更新成功"));
    }

    @Test
    void testUpdateItem_Fail() throws Exception {
        when(orderItemService.updateOrderItem(eq(2L), any(OrderItem.class)))
                .thenReturn(false);

        mockMvc.perform(put("/order-items/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderItem())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("更新失败"));
    }

    @Test
    void testDeleteItem_Success() throws Exception {
        when(orderItemService.deleteOrderItem(1L)).thenReturn(true);

        mockMvc.perform(delete("/order-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    void testDeleteItem_Fail() throws Exception {
        when(orderItemService.deleteOrderItem(2L)).thenReturn(false);

        mockMvc.perform(delete("/order-items/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("删除失败"));
    }

    @Test
    void testCalculateTotalAmount() throws Exception {
        when(orderItemService.calculateTotalAmountByOrderId(1L))
                .thenReturn(BigDecimal.valueOf(99.99));

        mockMvc.perform(get("/order-items/total/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(99.99));
    }

    @Test
    void testAddBatch() throws Exception {
        List<OrderItem> mockList = List.of(new OrderItem());
        when(orderItemService.addOrderItemsInBatch(anyList())).thenReturn(mockList);

        mockMvc.perform(post("/order-items/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteBatch_Success() throws Exception {
        when(orderItemService.deleteOrderItemsInBatch(anyList())).thenReturn(true);

        List<Long> ids = List.of(1L, 2L, 3L);
        mockMvc.perform(delete("/order-items/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量删除成功"));
    }

    @Test
    void testDeleteBatch_Fail() throws Exception {
        when(orderItemService.deleteOrderItemsInBatch(anyList())).thenReturn(false);

        List<Long> ids = List.of(1L, 2L);
        mockMvc.perform(delete("/order-items/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("批量删除失败"));
    }
    @Test
    void testGetProductIdsByUserId() throws Exception {
        Long userId = 1L;
        List<Long> mockProductIds = List.of(1L, 2L, 3L);

        when(orderItemService.getProductIdsByUserId(userId)).thenReturn(mockProductIds);

        mockMvc.perform(get("/order-items/user/{userId}/product-ids", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(2))
                .andExpect(jsonPath("$[2]").value(3));
    }
}
