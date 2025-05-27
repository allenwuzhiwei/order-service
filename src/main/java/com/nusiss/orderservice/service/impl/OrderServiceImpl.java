package com.nusiss.orderservice.service.impl;

import com.nusiss.commonservice.entity.*;
import com.nusiss.orderservice.dto.CreateOrderFromCartRequest;
import com.nusiss.orderservice.dto.DirectOrderRequest;
import com.nusiss.orderservice.entity.Order;
import com.nusiss.orderservice.dao.OrderRepository;
import com.nusiss.orderservice.entity.OrderItem;
import com.nusiss.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.nusiss.orderservice.dao.OrderItemRepository;
import com.nusiss.commonservice.config.ApiResponse;
import com.nusiss.commonservice.feign.ProductFeignClient;
import com.nusiss.commonservice.feign.InventoryFeignClient;
import com.nusiss.commonservice.feign.PaymentFeignClient;
import com.nusiss.commonservice.feign.ShoppingCartFeignClient;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/*
 OrderServiceImpl 实现类 - 提供订单模块的业务逻辑实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private InventoryFeignClient inventoryFeignClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    @Autowired
    private ShoppingCartFeignClient shoppingCartFeignClient;

    @Override
    public Order createDirectOrder(DirectOrderRequest request) {
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        // ===== 1. 获取商品详情（product-service）=====
        ApiResponse<Product> productRes = productFeignClient.getProductById(productId);
        if (!productRes.isSuccess() || productRes.getData() == null) {
            throw new RuntimeException("商品不存在或无法获取商品信息");
        }
        Product product = productRes.getData();

        // ===== 2. 获取库存信息（inventory-service）=====
        ApiResponse<Integer> stockRes = inventoryFeignClient.getInventoryQuantity(productId);
        if (!stockRes.isSuccess() || stockRes.getData() == null) {
            throw new RuntimeException("无法获取库存信息");
        }

        Integer availableStock = stockRes.getData();
        if (availableStock < quantity) {
            throw new RuntimeException("库存不足，无法下单");
        }

        // ===== 3. 先创建订单（此时支付状态为 UNPAID）=====
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setOrderStatus("CREATED");
        order.setPaymentStatus("UNPAID");
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(request.getShippingAddress());
        order.setCreateDatetime(LocalDateTime.now());
        order.setCreateUser("system");

        order = orderRepository.save(order); // 此时 orderId 已生成 ✅

        // ===== 4. 调用 payment-service 发起支付（使用真实 orderId）=====
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getOrderId());
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setAmount(totalAmount);
        paymentRequest.setCurrency("CNY");
        paymentRequest.setMethod("WeChat");

        ApiResponse<Payment> paymentRes = paymentFeignClient.processPayment(paymentRequest);

        if (!paymentRes.isSuccess() || paymentRes.getData() == null ||
                !"PAID".equalsIgnoreCase(paymentRes.getData().getPaymentStatus())) {
            throw new RuntimeException("支付失败，订单未创建");
        }

        // ===== 5. 更新订单为已支付状态 =====
        order.setPaymentStatus("PAID");
        order.setUpdateUser("system");
        order.setUpdateDatetime(LocalDateTime.now());
        orderRepository.save(order);

        // ===== 6. 扣减库存（inventory-service）=====
        InventoryChangeRequest changeRequest = new InventoryChangeRequest();
        changeRequest.setProductId(productId);
        changeRequest.setQuantity(quantity);
        changeRequest.setOperator("order-service");

        ApiResponse<Boolean> deductRes = inventoryFeignClient.deductInventory(changeRequest);
        if (!deductRes.isSuccess() || Boolean.FALSE.equals(deductRes.getData())) {
            throw new RuntimeException("扣减库存失败");
        }

        // ===== 7. 创建订单项 =====
        OrderItem item = new OrderItem();
        item.setOrderId(order.getOrderId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setQuantity(quantity);
        item.setProductPrice(product.getPrice());
        item.setSubtotalAmount(totalAmount);
        item.setCreateDatetime(LocalDateTime.now());
        item.setCreateUser("system");

        orderItemRepository.save(item);

        return order;
    }

    /*
     废用的方法实现，创建订单
     */
//    @Override
//    public Order createOrder(Order order) {
//        return orderRepository.save(order);
//    }
    /*
     根据订单 ID 查询
     */
    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /*
     查询所有订单
     */
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /*
     更新订单信息（如果存在该订单则更新）
     */
    @Override
    public boolean updateOrder(Order order) {
        if (orderRepository.existsById(order.getOrderId())) {
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    /*
     删除订单
     */
    @Override
    public boolean deleteOrder(Long orderId) {
        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
            return true;
        }
        return false;
    }


    /*
     扩展功能1：根据用户 ID 查询该用户的所有订单
     */
    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /*
     扩展功能2：多条件筛选订单（如状态、时间范围、金额范围）
     */
    @Override
    public List<Order> filterOrders(String status, Date startDate, Date endDate, Double minAmount, Double maxAmount) {
        List<Order> all = orderRepository.findAll();
        List<Order> filtered = new ArrayList<>();

        // 将 java.util.Date 转换为 java.time.LocalDateTime
        LocalDateTime startDateTime = (startDate != null) ? startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;


        for (Order order : all) {
            boolean match = true;

            if (status != null && !status.equals(order.getOrderStatus())) {
                match = false;
            }
            if (startDate != null && order.getOrderDate().isBefore(startDateTime)) {
                match = false;
            }
            if (endDate != null && order.getOrderDate().isAfter(endDateTime)) {
                match = false;
            }
            // 修改 filterOrders 方法中的 minAmount 和 maxAmount 比较逻辑

            if (minAmount != null && order.getTotalAmount().compareTo(BigDecimal.valueOf(minAmount)) < 0) {
                match = false;
            }
            if (maxAmount != null && order.getTotalAmount().compareTo(BigDecimal.valueOf(maxAmount)) > 0) {
                match = false;
            }

            if (match) {
                filtered.add(order);
            }
        }

        return filtered;
    }

    /*
     扩展功能3：分页获取订单列表，并按指定字段排序
     */
    @Override
    public List<Order> getOrdersWithPaginationAndSorting(int page, int size, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Order> resultPage = orderRepository.findAll(pageable);
        return resultPage.getContent();
    }

}
