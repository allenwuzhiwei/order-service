package com.nusiss.orderservice.dao;

import com.nusiss.orderservice.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 OrderPaymentRepository - 操作 order_payments 表的 JPA 数据访问接口
 */
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    // 根据订单ID查询所有支付记录（一个订单可能有多个支付行为）
    List<OrderPayment> findByOrderId(Long orderId);

}
