package com.nusiss.orderservice.service.impl;

import com.nusiss.orderservice.dao.OrderPaymentRepository;
import com.nusiss.orderservice.entity.OrderPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderPaymentServiceImplTest {

    @Mock
    private OrderPaymentRepository paymentRepository;

    @InjectMocks
    private OrderPaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePayment() {
        OrderPayment payment = new OrderPayment();
        when(paymentRepository.save(any())).thenReturn(payment);

        OrderPayment result = paymentService.createPayment(payment);

        assertNotNull(result);
        verify(paymentRepository).save(payment);
        assertNotNull(payment.getCreateDatetime());
    }

    @Test
    void testGetPaymentById_Found() {
        OrderPayment payment = new OrderPayment();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Optional<OrderPayment> result = paymentService.getPaymentById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testGetPaymentById_NotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<OrderPayment> result = paymentService.getPaymentById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of(new OrderPayment()));

        List<OrderPayment> result = paymentService.getAllPayments();
        assertEquals(1, result.size());
    }

    @Test
    void testUpdatePayment_Success() {
        OrderPayment payment = new OrderPayment();
        payment.setPaymentId(1L);

        when(paymentRepository.existsById(1L)).thenReturn(true);
        when(paymentRepository.save(any())).thenReturn(payment);

        boolean result = paymentService.updatePayment(payment);
        assertTrue(result);
        verify(paymentRepository).save(payment);
        assertNotNull(payment.getUpdateDatetime());
    }

    @Test
    void testUpdatePayment_NotFound() {
        OrderPayment payment = new OrderPayment();
        payment.setPaymentId(2L);

        when(paymentRepository.existsById(2L)).thenReturn(false);

        boolean result = paymentService.updatePayment(payment);
        assertFalse(result);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void testDeletePayment_Success() {
        when(paymentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(paymentRepository).deleteById(1L);

        boolean result = paymentService.deletePayment(1L);
        assertTrue(result);
        verify(paymentRepository).deleteById(1L);
    }

    @Test
    void testDeletePayment_NotFound() {
        when(paymentRepository.existsById(2L)).thenReturn(false);

        boolean result = paymentService.deletePayment(2L);
        assertFalse(result);
        verify(paymentRepository, never()).deleteById(any());
    }

    @Test
    void testGetPaymentsByOrderId() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(new OrderPayment()));

        List<OrderPayment> result = paymentService.getPaymentsByOrderId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testFilterPayments() {
        OrderPayment p1 = new OrderPayment();
        p1.setPaymentStatus("SUCCESS");
        p1.setPaymentMethod("WeChat");
        p1.setPaymentDate(LocalDateTime.of(2024, 1, 10, 10, 0));

        when(paymentRepository.findAll()).thenReturn(List.of(p1));

        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, Calendar.JANUARY, 1);
        Date start = calendar.getTime();

        calendar.set(2024, Calendar.DECEMBER, 31);
        Date end = calendar.getTime();

        List<OrderPayment> result = paymentService.filterPayments("SUCCESS", "WeChat", start, end);
        assertEquals(1, result.size());
    }

    @Test
    void testCalculateTotalPaidByOrderId() {
        OrderPayment p1 = new OrderPayment();
        p1.setAmountPaid(88.88);
        OrderPayment p2 = new OrderPayment();
        p2.setAmountPaid(100.00);

        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(p1, p2));

        Double total = paymentService.calculateTotalPaidByOrderId(1L);
        assertEquals(188.88, total);
    }
}
