package com.nusiss.orderservice.service.strategy.impl;

import com.nusiss.orderservice.entity.OrderShipment;
import com.nusiss.orderservice.service.strategy.ShipmentStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("DELIVERED")
public class DeliveredShipmentStrategy implements ShipmentStrategy {
    @Override
    public void handleShipmentStatus(OrderShipment shipment) {
        if (shipment.getShippedDate() == null) {
            shipment.setShippedDate(LocalDateTime.now().minusDays(1)); // 可调整为合理时间
        }
        shipment.setDeliveryDate(LocalDateTime.now());
    }
}
