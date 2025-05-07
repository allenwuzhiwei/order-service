package com.nusiss.orderservice.service;

import com.nusiss.orderservice.entity.OrderShipment;

import java.util.List;
import java.util.Optional;

public interface OrderShipmentService {

    // 创建发货记录（使用策略处理发货状态逻辑）
    OrderShipment createShipment(OrderShipment shipment);

    // 更新发货记录（同样使用策略）
    boolean updateShipment(Long shipmentId, OrderShipment updatedShipment);

    // 删除发货记录
    boolean deleteShipment(Long shipmentId);

    // 根据订单 ID 查询发货记录
    Optional<OrderShipment> getShipmentByOrderId(Long orderId);

    // 获取所有发货记录（可选调试）
    List<OrderShipment> getAllShipments();
}
