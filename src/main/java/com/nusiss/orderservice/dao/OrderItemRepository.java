package com.nusiss.orderservice.dao;

import com.nusiss.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 OrderItemRepository - JPA 数据访问接口
 提供对 order_items 表的操作
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /*
     根据订单 ID 查询该订单的所有订单项
     @param orderId 所属订单 ID
     @return 商品项列表
     */
    List<OrderItem> findByOrderId(Long orderId);

    @Query(value = "SELECT oi.product_id FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "WHERE o.user_id = :userId", nativeQuery = true)
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);


}
