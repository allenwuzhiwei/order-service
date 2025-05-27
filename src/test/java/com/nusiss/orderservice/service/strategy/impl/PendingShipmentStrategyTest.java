package com.nusiss.orderservice.service.strategy.impl;

import com.nusiss.orderservice.entity.OrderShipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单元测试：PendingShipmentStrategy
 */
class PendingShipmentStrategyTest {

    private PendingShipmentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PendingShipmentStrategy();
    }

    @Test
    void testHandleShipmentStatus_ShouldClearDates() {
        // 准备包含发货和送达时间的发货记录
        OrderShipment shipment = new OrderShipment();
        shipment.setShippedDate(LocalDateTime.now().minusDays(2));
        shipment.setDeliveryDate(LocalDateTime.now().minusDays(1));

        // 执行策略
        strategy.handleShipmentStatus(shipment);

        // 断言两个时间都被清除
        assertNull(shipment.getShippedDate(), "ShippedDate 应该被清空");
        assertNull(shipment.getDeliveryDate(), "DeliveryDate 应该被清空");
    }
}
