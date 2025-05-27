package com.nusiss.orderservice.service.strategy.impl;

import com.nusiss.orderservice.entity.OrderShipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单元测试：ShippedShipmentStrategy
 */
class ShippedShipmentStrategyTest {

    private ShippedShipmentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ShippedShipmentStrategy();
    }

    @Test
    void testHandleShipmentStatus_ShouldSetShippedDateAndClearDeliveryDate() {
        // 准备含旧 deliveryDate 的对象
        OrderShipment shipment = new OrderShipment();
        shipment.setDeliveryDate(LocalDateTime.now().minusDays(1));

        // 执行策略
        strategy.handleShipmentStatus(shipment);

        // 验证 shippedDate 设置为当前时间（误差允许）
        assertNotNull(shipment.getShippedDate(), "shippedDate 应被设置");
        assertTrue(
                shipment.getShippedDate().isBefore(LocalDateTime.now().plusSeconds(2)),
                "shippedDate 应接近当前时间"
        );

        // 验证 deliveryDate 被清空
        assertNull(shipment.getDeliveryDate(), "deliveryDate 应被清空");
    }
}
