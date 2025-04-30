package com.nusiss.orderservice.service.impl;

public class OrderItemServiceImpl {
import com.nusiss.orderservice.dao.OrderItemRepository;
import com.nusiss.orderservice.entity.OrderItem;
import com.nusiss.orderservice.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    // 添加商品项至订单
    @Override
    public OrderItem addOrderItem(OrderItem item) {
        item.setSubtotalAmount(item.getProductPrice() * item.getQuantity());
        return orderItemRepository.save(item);
    }

    // 根据 orderId 获取某个订单下所有商品项
    @Override
    public List<OrderItem> getItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // 修改某个商品项（如数量）
    @Override
    public boolean updateOrderItem(Long itemId, OrderItem updatedItem) {
        if (orderItemRepository.existsById(itemId)) {
            updatedItem.setItemId(itemId); // 确保使用传入的 ID
            orderItemRepository.save(updatedItem);
            return true;
        }
        return false;
    }

    // 删除某个商品项
    @Override
    public boolean deleteOrderItem(Long itemId) {
        if (orderItemRepository.existsById(itemId)) {
            orderItemRepository.deleteById(itemId);
            return true;
        }
        return false;
    }
}
