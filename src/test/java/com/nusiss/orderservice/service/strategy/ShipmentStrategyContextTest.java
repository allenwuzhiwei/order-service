package com.nusiss.orderservice.service.strategy;

import com.nusiss.orderservice.entity.OrderShipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 单元测试：ShipmentStrategyContext
 */
class ShipmentStrategyContextTest {

    private ShipmentStrategyContext strategyContext;
    private ShipmentStrategy mockPendingStrategy;
    private ShipmentStrategy mockShippedStrategy;

    @BeforeEach
    void setUp() {
        // 模拟两个策略
        mockPendingStrategy = mock(ShipmentStrategy.class);
        mockShippedStrategy = mock(ShipmentStrategy.class);

        // 构造映射关系
        Map<String, ShipmentStrategy> strategyMap = Map.of(
                "PENDING", mockPendingStrategy,
                "SHIPPED", mockShippedStrategy
        );

        strategyContext = new ShipmentStrategyContext(strategyMap);
    }

    @Test
    void testExecuteStrategy_ShouldCallCorrectStrategy() {
        // 模拟发货对象
        OrderShipment shipment = new OrderShipment();
        shipment.setShipmentStatus("PENDING");

        // 执行策略
        strategyContext.executeStrategy(shipment);

        // 验证正确策略被调用
        verify(mockPendingStrategy, times(1)).handleShipmentStatus(shipment);
        verify(mockShippedStrategy, never()).handleShipmentStatus(any());
    }

    @Test
    void testExecuteStrategy_ShouldThrowExceptionWhenStrategyNotFound() {
        // 模拟不存在的发货状态
        OrderShipment shipment = new OrderShipment();
        shipment.setShipmentStatus("UNKNOWN");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                strategyContext.executeStrategy(shipment)
        );

        assertTrue(ex.getMessage().contains("Unsupported shipment status"));
    }
}
