package com.nusiss.orderservice.service.impl;

import com.nusiss.orderservice.dao.OrderShipmentRepository;
import com.nusiss.orderservice.entity.OrderShipment;
import com.nusiss.orderservice.service.OrderShipmentService;
import com.nusiss.orderservice.service.strategy.ShipmentStrategyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 订单发货服务实现类
 */
@Service
public class OrderShipmentServiceImpl implements OrderShipmentService {

    @Autowired
    private OrderShipmentRepository shipmentRepository;

    @Autowired
    private ShipmentStrategyContext strategyContext;

    /*
     创建订单发货记录
     @param shipment 订单发货对象
     @return 保存后的订单发货对象
     */
    @Override
    public OrderShipment createShipment(OrderShipment shipment) {
        shipment.setCreateDatetime(LocalDateTime.now());
        strategyContext.executeStrategy(shipment); // 应用策略
        return shipmentRepository.save(shipment);
    }

    /*
     更新订单发货记录
     @param shipmentId 订单发货ID
     @param updatedShipment 更新后的订单发货对象
     @return 更新是否成功
     */
    @Override
    public boolean updateShipment(Long shipmentId, OrderShipment updatedShipment) {
        if (shipmentRepository.existsById(shipmentId)) {
            updatedShipment.setShipmentId(shipmentId);
            updatedShipment.setUpdateDatetime(LocalDateTime.now());
            strategyContext.executeStrategy(updatedShipment); // 应用策略
            shipmentRepository.save(updatedShipment);
            return true;
        }
        return false;
    }

    /*
     删除订单发货记录
     @param shipmentId 订单发货ID
     @return 删除是否成功
     */
    @Override
    public boolean deleteShipment(Long shipmentId) {
        if (shipmentRepository.existsById(shipmentId)) {
            shipmentRepository.deleteById(shipmentId);
            return true;
        }
        return false;
    }

    /*
     根据订单ID获取订单发货记录
     @param orderId 订单ID
     @return 订单发货对象，如果不存在则返回空的Optional
     */
    @Override
    public Optional<OrderShipment> getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId);  // 返回的是 Optional
    }

    /*
     获取所有订单发货记录
     @return 订单发货列表
     */
    @Override
    public List<OrderShipment> getAllShipments() {
        return shipmentRepository.findAll();
    }
}
