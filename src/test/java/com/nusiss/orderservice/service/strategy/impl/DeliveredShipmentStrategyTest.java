package com.nusiss.orderservice.service.strategy.impl;

import com.nusiss.orderservice.entity.OrderShipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单元测试：DeliveredShipmentStrategy
 */
class DeliveredShipmentStrategyTest {

    private DeliveredShipmentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DeliveredShipmentStrategy();
    }

    @Test
    void testHandleShipmentStatus_WhenShippedDateIsNull() {
        OrderShipment shipment = new OrderShipment();
        shipment.setShipmentStatus("DELIVERED");

        strategy.handleShipmentStatus(shipment);

        assertNotNull(shipment.getShippedDate(), "ShippedDate 应该被设置");
        assertNotNull(shipment.getDeliveryDate(), "DeliveryDate 应该被设置");
        assertTrue(shipment.getShippedDate().isBefore(shipment.getDeliveryDate()));
    }

    @Test
    void testHandleShipmentStatus_WhenShippedDateAlreadyExists() {
        OrderShipment shipment = new OrderShipment();
        LocalDateTime shippedTime = LocalDateTime.now().minusDays(3);
        shipment.setShippedDate(shippedTime);

        strategy.handleShipmentStatus(shipment);

        assertEquals(shippedTime, shipment.getShippedDate(), "已有 ShippedDate 不应被覆盖");
        assertNotNull(shipment.getDeliveryDate(), "DeliveryDate 应该被设置");
    }
}
