package com.nusiss.orderservice.service.impl;

import com.nusiss.orderservice.dao.OrderItemRepository;
import com.nusiss.orderservice.entity.OrderItem;
import com.nusiss.orderservice.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;


    /* 添加商品项至订单，暂时弃用*/
//    @Override
//    public OrderItem addOrderItem(OrderItem item) {
//        item.setSubtotalAmount(item.getProductPrice() * item.getQuantity());
//        return orderItemRepository.save(item);
//    }

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

    // 扩展功能：获取某订单下所有商品的总金额
    @Override
    public BigDecimal calculateTotalAmountByOrderId(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return items.stream()
                .map(OrderItem::getSubtotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // 扩展功能：批量添加
    @Override
    public List<OrderItem> addOrderItemsInBatch(List<OrderItem> items) {
        return orderItemRepository.saveAll(items);
    }

    // 扩展功能：批量删除
    @Override
    public boolean deleteOrderItemsInBatch(List<Long> itemIds) {
        try {
            orderItemRepository.deleteAllById(itemIds);
            return true; // 删除成功
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 删除失败
        }
    }
}
