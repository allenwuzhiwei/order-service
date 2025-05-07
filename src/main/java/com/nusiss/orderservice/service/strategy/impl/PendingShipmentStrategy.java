package com.nusiss.orderservice.service.strategy.impl;

import com.nusiss.orderservice.entity.OrderShipment;
import com.nusiss.orderservice.service.strategy.ShipmentStrategy;
import org.springframework.stereotype.Component;

@Component("PENDING")
public class PendingShipmentStrategy implements ShipmentStrategy {
    @Override
    public void handleShipmentStatus(OrderShipment shipment) {
        // 处理未发货逻辑（例如校验、设置默认值等）
        shipment.setShippedDate(null);
        shipment.setDeliveryDate(null);
    }
}
