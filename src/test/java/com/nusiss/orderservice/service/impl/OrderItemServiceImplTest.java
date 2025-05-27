package com.nusiss.orderservice.service.impl;

import com.nusiss.orderservice.dao.OrderItemRepository;
import com.nusiss.orderservice.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetItemsByOrderId() {
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(new OrderItem()));

        List<OrderItem> result = orderItemService.getItemsByOrderId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateOrderItem_Success() {
        OrderItem item = new OrderItem();
        when(orderItemRepository.existsById(1L)).thenReturn(true);
        when(orderItemRepository.save(any())).thenReturn(item);

        boolean result = orderItemService.updateOrderItem(1L, item);
        assertTrue(result);
        verify(orderItemRepository, times(1)).save(item);
    }

    @Test
    void testUpdateOrderItem_NotFound() {
        when(orderItemRepository.existsById(2L)).thenReturn(false);

        boolean result = orderItemService.updateOrderItem(2L, new OrderItem());
        assertFalse(result);
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void testDeleteOrderItem_Success() {
        when(orderItemRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderItemRepository).deleteById(1L);

        boolean result = orderItemService.deleteOrderItem(1L);
        assertTrue(result);
        verify(orderItemRepository).deleteById(1L);
    }

    @Test
    void testDeleteOrderItem_NotFound() {
        when(orderItemRepository.existsById(2L)).thenReturn(false);

        boolean result = orderItemService.deleteOrderItem(2L);
        assertFalse(result);
    }

    @Test
    void testCalculateTotalAmountByOrderId() {
        OrderItem i1 = new OrderItem();
        i1.setSubtotalAmount(BigDecimal.valueOf(10.5));
        OrderItem i2 = new OrderItem();
        i2.setSubtotalAmount(BigDecimal.valueOf(20.5));

        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(i1, i2));

        BigDecimal total = orderItemService.calculateTotalAmountByOrderId(1L);
        assertEquals(BigDecimal.valueOf(31.0), total);
    }

    @Test
    void testAddOrderItemsInBatch() {
        List<OrderItem> items = List.of(new OrderItem(), new OrderItem());
        when(orderItemRepository.saveAll(items)).thenReturn(items);

        List<OrderItem> result = orderItemService.addOrderItemsInBatch(items);
        assertEquals(2, result.size());
    }

    @Test
    void testDeleteOrderItemsInBatch_Success() {
        doNothing().when(orderItemRepository).deleteAllById(anyList());

        boolean result = orderItemService.deleteOrderItemsInBatch(List.of(1L, 2L));
        assertTrue(result);
    }

    @Test
    void testDeleteOrderItemsInBatch_Exception() {
        doThrow(new RuntimeException("fail")).when(orderItemRepository).deleteAllById(anyList());

        boolean result = orderItemService.deleteOrderItemsInBatch(List.of(1L, 2L));
        assertFalse(result);
    }
}
