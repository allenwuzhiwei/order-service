package com.nusiss.orderservice.dao;

import com.nusiss.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 OrderRepository 接口用于定义与订单（Order）相关的数据库访问操作。
 继承 JpaRepository 可自动获得常用的 CRUD 方法，无需手动实现。
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /*
     根据用户 ID 查找所有订单记录
     @param userId 用户 ID
     @return 属于该用户的订单列表
     */
    List<Order> findByUserId(Long userId);

    /*
     根据订单状态查询订单
     @param orderStatus 订单状态（如已付款、已发货等）
     @return 对应状态的订单列表
     */
    List<Order> findByOrderStatus(String orderStatus);

    /*
     chatbox 用
     */
    // 查询该用户的所有订单，按创建时间倒序排列
    List<Order> findByUserIdOrderByCreateDatetimeDesc(Long userId);

    // 查询该用户的订单总数
    int countByUserId(Long userId);

    // 查询该用户指定状态的订单数量
    int countByUserIdAndOrderStatus(Long userId, String status);
}
