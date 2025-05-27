package com.nusiss.orderservice.service.impl;

import com.nusiss.orderservice.dao.OrderShipmentRepository;
import com.nusiss.orderservice.entity.OrderShipment;
import com.nusiss.orderservice.service.strategy.ShipmentStrategyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderShipmentServiceImplTest {

    @InjectMocks
    private OrderShipmentServiceImpl shipmentService;

    @Mock
    private OrderShipmentRepository shipmentRepository;

    @Mock
    private ShipmentStrategyContext strategyContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateShipment() {
        OrderShipment shipment = new OrderShipment();
        when(shipmentRepository.save(any(OrderShipment.class))).thenReturn(shipment);

        OrderShipment result = shipmentService.createShipment(shipment);

        assertNotNull(result);
        verify(strategyContext).executeStrategy(shipment);
        verify(shipmentRepository).save(shipment);
        assertNotNull(shipment.getCreateDatetime());
    }

    @Test
    void testUpdateShipment_Found() {
        Long id = 1L;
        OrderShipment shipment = new OrderShipment();
        shipment.setShipmentStatus("SHIPPED");

        when(shipmentRepository.existsById(id)).thenReturn(true);
        when(shipmentRepository.save(any(OrderShipment.class))).thenReturn(shipment);

        boolean updated = shipmentService.updateShipment(id, shipment);

        assertTrue(updated);
        assertEquals(id, shipment.getShipmentId());
        verify(strategyContext).executeStrategy(shipment);
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void testUpdateShipment_NotFound() {
        Long id = 1L;
        OrderShipment shipment = new OrderShipment();

        when(shipmentRepository.existsById(id)).thenReturn(false);

        boolean updated = shipmentService.updateShipment(id, shipment);

        assertFalse(updated);
        verify(shipmentRepository, never()).save(any());
        verify(strategyContext, never()).executeStrategy(any());
    }

    @Test
    void testDeleteShipment_Success() {
        Long id = 1L;
        when(shipmentRepository.existsById(id)).thenReturn(true);
        doNothing().when(shipmentRepository).deleteById(id);

        boolean deleted = shipmentService.deleteShipment(id);

        assertTrue(deleted);
        verify(shipmentRepository).deleteById(id);
    }

    @Test
    void testDeleteShipment_NotFound() {
        Long id = 1L;
        when(shipmentRepository.existsById(id)).thenReturn(false);

        boolean deleted = shipmentService.deleteShipment(id);

        assertFalse(deleted);
        verify(shipmentRepository, never()).deleteById(any());
    }

    @Test
    void testGetShipmentByOrderId_Found() {
        Long orderId = 10L;
        OrderShipment shipment = new OrderShipment();
        shipment.setOrderId(orderId);

        when(shipmentRepository.findByOrderId(orderId)).thenReturn(Optional.of(shipment));

        Optional<OrderShipment> result = shipmentService.getShipmentByOrderId(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getOrderId());
    }

    @Test
    void testGetShipmentByOrderId_NotFound() {
        when(shipmentRepository.findByOrderId(anyLong())).thenReturn(Optional.empty());

        Optional<OrderShipment> result = shipmentService.getShipmentByOrderId(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllShipments() {
        List<OrderShipment> mockList = Arrays.asList(new OrderShipment(), new OrderShipment());
        when(shipmentRepository.findAll()).thenReturn(mockList);

        List<OrderShipment> result = shipmentService.getAllShipments();

        assertEquals(2, result.size());
    }
}
