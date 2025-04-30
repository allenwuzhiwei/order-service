package com.nusiss.orderservice.service;

import com.nusiss.orderservice.entity.OrderItem;

import java.util.List;
import java.util.Optional;

/*
 OrderItemService 接口 - 定义订单项相关的业务逻辑
 */
public interface OrderItemService {

    // 基础功能

    /*
     添加一个商品项到订单
     @param orderItem 要添加的订单项对象
     @return 创建成功后的订单项
     */
    OrderItem addOrderItem(OrderItem orderItem);

    /*
     获取指定订单的所有订单项
     @param orderId 订单ID
     @return 对应订单的商品项列表
     */
    List<OrderItem> getItemsByOrderId(Long orderId);

    /*
     更新订单项信息（如数量等）
     @param itemId 订单项ID
     @param updatedItem 更新的字段内容（如数量）
     @return 是否更新成功
     */
    boolean updateOrderItem(Long itemId, OrderItem updatedItem);

    /*
     删除订单中的某一项商品
     @param itemId 要删除的订单项ID
     @return 是否删除成功
     */
    boolean deleteOrderItem(Long itemId);

}
