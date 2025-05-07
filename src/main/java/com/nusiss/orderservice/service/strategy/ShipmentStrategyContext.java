package com.nusiss.orderservice.service.strategy;

import com.nusiss.orderservice.entity.OrderShipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/*
 发货策略上下文类，用于动态选择并执行不同的发货策略
 通过Spring的自动注入，获取所有发货策略的映射，根据订单发货状态选择合适的策略进行处理
 */
@Component
public class ShipmentStrategyContext {

    // 存储发货策略的映射，键为发货状态，值为对应的发货策略实现类
    private final Map<String, ShipmentStrategy> strategyMap;

    /*
     构造函数，通过自动注入初始化策略映射
     @param strategyMap 由Spring框架自动注入的策略映射，包含所有已定义的发货策略
     */
    @Autowired
    public ShipmentStrategyContext(Map<String, ShipmentStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    /*
     执行与订单发货状态相匹配的策略
     根据订单的发货状态，选择相应的策略实现类，并调用其处理方法
     如果没有找到匹配的策略，则抛出异常
     @param shipment 订单发货信息，包含发货状态和其他相关信息
     @throws IllegalArgumentException 如果订单的发货状态没有对应的策略实现，抛出此异常
     */
    public void executeStrategy(OrderShipment shipment) {
        String status = shipment.getShipmentStatus();
        ShipmentStrategy strategy = strategyMap.get(status);
        if (strategy != null) {
            strategy.handleShipmentStatus(shipment);
        } else {
            throw new IllegalArgumentException("Unsupported shipment status: " + status);
        }
    }
}
