package com.nusiss.orderservice.service.strategy;

import com.nusiss.orderservice.entity.OrderShipment;

public interface ShipmentStrategy {
    void handleShipmentStatus(OrderShipment shipment);
}
