package com.nusiss.orderservice.dao;

import com.nusiss.orderservice.entity.OrderShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 OrderShipmentRepository - 操作 order_shipments 表的 JPA 数据访问接口
 */
@Repository
public interface OrderShipmentRepository extends JpaRepository<OrderShipment, Long> {

    // 根据订单 ID 查询该订单的所有发货记录（一个订单对应一个发货记录）
    Optional<OrderShipment> findByOrderId(Long orderId);

}
