package com.nusiss.orderservice.service.strategy.impl;

import com.nusiss.orderservice.entity.OrderShipment;
import com.nusiss.orderservice.service.strategy.ShipmentStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("SHIPPED")
public class ShippedShipmentStrategy implements ShipmentStrategy {
    @Override
    public void handleShipmentStatus(OrderShipment shipment) {
        shipment.setShippedDate(LocalDateTime.now());
        shipment.setDeliveryDate(null);
    }
}
