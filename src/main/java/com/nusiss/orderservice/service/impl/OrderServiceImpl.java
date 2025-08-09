package com.nusiss.orderservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nusiss.commonservice.entity.*;
import com.nusiss.orderservice.dto.CreateOrderFromCartRequest;
import com.nusiss.orderservice.dto.DirectOrderRequest;
import com.nusiss.orderservice.dto.FacePaymentDirectOrderRequest;
import com.nusiss.orderservice.entity.Order;
import com.nusiss.orderservice.dao.OrderRepository;
import com.nusiss.orderservice.entity.OrderItem;
import com.nusiss.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nusiss.orderservice.dao.OrderItemRepository;
import com.nusiss.commonservice.config.ApiResponse;
import com.nusiss.commonservice.feign.ProductFeignClient;
import com.nusiss.commonservice.feign.InventoryFeignClient;
import com.nusiss.commonservice.feign.PaymentFeignClient;
import com.nusiss.commonservice.feign.ShoppingCartFeignClient;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/*
 OrderServiceImpl 实现类 - 提供订单模块的业务逻辑实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final String SYSTEM_USER = "system"; // 🔧 提取常量避免硬编码重复

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

    /*
     直接下单逻辑-普通支付
     */
    @Override
    public Order createDirectOrder(DirectOrderRequest request) {
        return createDirectOrderInternal(request);
    }

    /*
     直接下单逻辑-人脸识别支付
     */
    @Override
    public Order createOrderWithFaceRecognition(FacePaymentDirectOrderRequest request, MultipartFile faceImage) {
        // Step 1: 调用人脸识别服务
        ResponseEntity<String> response = paymentFeignClient.verifyFace(faceImage);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("调用人脸识别服务失败");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (!"200".equals(root.get("status").asText())) {
                throw new RuntimeException("人脸识别失败：" + root.get("message").asText());
            }

            Long userId = Long.parseLong(root.get("userId").asText());
            System.out.println("人脸识别返回的 userId: " + userId);
            request.setUserId(userId); // 注入识别出的 userId

        } catch (Exception e) {
            throw new RuntimeException("解析人脸识别结果失败", e);
        }

        // Step 2: 调用共用下单逻辑
        return createDirectOrderInternal(request);
    }



    /*
     核心直接下单逻辑（供普通支付、人脸支付复用）
     */
    private Order createDirectOrderInternal(DirectOrderRequest request) {
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        // ===== 1. 获取商品详情 (product-service) =====
        ApiResponse<Product> productRes = productFeignClient.getProductById(productId);
        if (!productRes.isSuccess() || productRes.getData() == null) {
            throw new RuntimeException("商品不存在或无法获取商品信息");
        }
        Product product = productRes.getData();

        // ===== 2. 获取库存信息 (inventory-service) =====
        ApiResponse<Integer> stockRes = inventoryFeignClient.getInventoryQuantity(productId);
        if (!stockRes.isSuccess() || stockRes.getData() == null) {
            throw new RuntimeException("无法获取库存信息");
        }
        Integer availableStock = stockRes.getData();
        if (availableStock < quantity) {
            throw new RuntimeException("库存不足，无法下单");
        }

        // ===== 3. 创建订单（状态为 UNPAID） =====
        Order order = new Order();
        order.setUserId(request.getUserId()); // 绑定用户ID
        order.setOrderDate(LocalDateTime.now()); // 下单时间
        order.setOrderStatus("CREATED");
        order.setPaymentStatus("UNPAID");
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(request.getShippingAddress());
        order.setCreateDatetime(LocalDateTime.now());
        order.setCreateUser(SYSTEM_USER);

        order = orderRepository.save(order); // 此时 orderId 已生成

        // ===== 4. 调用 payment-service 发起支付 =====
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getOrderId());
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setAmount(totalAmount);
        paymentRequest.setMethod(request.getPaymentMethod());

        // 设置币种
        String method = request.getPaymentMethod();
        String currency;
        switch (method) {
            case "WeChat":
            case "PayNow":
            case "FaceRecognition":
                currency = "SGD";
                break;
            default:
                currency = "CNY";
        }
        paymentRequest.setCurrency(currency);

        // 设置卖家信息
        paymentRequest.setProductId(product.getId());
        paymentRequest.setSellerId(product.getSellerId());

        ApiResponse<Payment> paymentRes = paymentFeignClient.processPayment(paymentRequest);
        if (!paymentRes.isSuccess() || paymentRes.getData() == null ||
                !"PAID".equalsIgnoreCase(paymentRes.getData().getPaymentStatus())) {
            throw new RuntimeException("支付失败，订单未创建");
        }

        // ===== 5. 更新订单为已支付状态 =====
        order.setPaymentStatus("PAID");
        order.setUpdateUser(SYSTEM_USER);
        order.setUpdateDatetime(LocalDateTime.now());
        orderRepository.save(order);

        // ===== 6. 扣减库存 (inventory-service) =====
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
        item.setProductPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setSubtotalAmount(totalAmount);
        item.setCreateDatetime(LocalDateTime.now());
        item.setCreateUser(SYSTEM_USER);

        orderItemRepository.save(item);

        return order;
    }

    @Override
    public Order createOrderFromCart(CreateOrderFromCartRequest request) {
//        overrideUserIdIfFaceRecognitionEnabled(request);
        Long userId = request.getUserId();
        String shippingAddress = request.getShippingAddress();
        String paymentMethod = request.getPaymentMethod();

        List<CartItem> cartItems = getValidatedCartItems(userId);
        validateStock(cartItems);
        Map<Long, Product> productMap = new HashMap<>();
        BigDecimal totalAmount = calculateTotalAmount(cartItems, productMap);

        Order order = createAndSaveOrder(userId, shippingAddress, totalAmount);
        processPayment(order, userId, totalAmount, paymentMethod, productMap);
        createOrderItems(order, cartItems, productMap);
        deductInventory(cartItems);

        shoppingCartFeignClient.clearCart(userId);
        return order;
    }

    /*
     从购物车下单逻辑 - 人脸识别支付
     */
    @Override
    public Order createOrderFromCartWithFaceRecognition(CreateOrderFromCartRequest request, MultipartFile faceImage) {
        // Step 1：调用人脸识别服务
        ResponseEntity<String> response = paymentFeignClient.verifyFace(faceImage);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("调用人脸识别服务失败");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (!"200".equals(root.get("status").asText())) {
                throw new RuntimeException("人脸识别失败: " + root.get("message").asText());
            }

            Long userId = Long.parseLong(root.get("userId").asText());
            System.out.println("人脸识别返回的 userId: " + userId);

            request.setUserId(userId); // 注入识别出的用户ID

        } catch (Exception e) {
            throw new RuntimeException("解析人脸识别结果失败", e);
        }

        // Step 2：调用已有从购物车下单的主逻辑
        return createOrderFromCart(request);
    }


    private List<CartItem> getValidatedCartItems(Long userId) {
        ApiResponse<List<CartItem>> cartRes = shoppingCartFeignClient.getCartItems(userId);
        if (!cartRes.isSuccess() || cartRes.getData() == null || cartRes.getData().isEmpty()) {
            throw new RuntimeException("购物车为空，无法下单");
        }
        return cartRes.getData();
    }

    private void validateStock(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            ApiResponse<Integer> stockRes = inventoryFeignClient.getInventoryQuantity(item.getProductId());
            if (!stockRes.isSuccess() || stockRes.getData() == null) {
                throw new RuntimeException("无法获取商品库存，商品ID: " + item.getProductId());
            }
            if (stockRes.getData() < item.getQuantity()) {
                throw new RuntimeException("商品库存不足，商品ID: " + item.getProductId());
            }
        }
    }

    private BigDecimal calculateTotalAmount(List<CartItem> cartItems, Map<Long, Product> productMap) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            ApiResponse<Product> productRes = productFeignClient.getProductById(item.getProductId());
            if (!productRes.isSuccess() || productRes.getData() == null) {
                throw new RuntimeException("获取商品信息失败，商品ID: " + item.getProductId());
            }
            Product product = productRes.getData();
            productMap.put(product.getId(), product);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return totalAmount;
    }

    private Order createAndSaveOrder(Long userId, String shippingAddress, BigDecimal totalAmount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderStatus("CREATED");
        order.setPaymentStatus("UNPAID");
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setOrderDate(LocalDateTime.now()); // 设置下单时间
        order.setCreateDatetime(LocalDateTime.now());
        order.setCreateUser(SYSTEM_USER);
        return orderRepository.save(order);
    }

    private void processPayment(Order order, Long userId, BigDecimal totalAmount, String method, Map<Long, Product> productMap) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getOrderId());
        paymentRequest.setUserId(userId);
        paymentRequest.setAmount(totalAmount);
        paymentRequest.setMethod(method);

        // 从 productMap 获取任意一个商品作为支付信息（用于记录 productId 和 sellerId）
        Product product = productMap.values().stream().findFirst().orElse(null);
        if (product == null) {
            throw new RuntimeException("订单中无有效商品，无法发起支付");
        }
        paymentRequest.setProductId(product.getId());
        paymentRequest.setSellerId(product.getSellerId());

        // 根据支付方式设置币种
        String currency;
        switch (method) {
            case "WeChat":
            case "PayNow":
            case "PayLah":
//            case "FaceRecognition":
                currency = "SGD";
                break;
            default:
                currency = "CNY"; // 默认币种
        }
        paymentRequest.setCurrency(currency);

        ApiResponse<Payment> paymentRes = paymentFeignClient.processPayment(paymentRequest);
        if (!paymentRes.isSuccess() || paymentRes.getData() == null ||
                !"PAID".equalsIgnoreCase(paymentRes.getData().getPaymentStatus())) {
            // 可选：回滚订单或打日志
            orderRepository.deleteById(order.getOrderId());
            throw new RuntimeException("支付失败，订单未完成");
        }

        order.setPaymentStatus("PAID");
        order.setUpdateUser(SYSTEM_USER);
        order.setUpdateDatetime(LocalDateTime.now());
        orderRepository.save(order);
    }

    private void createOrderItems(Order order, List<CartItem> cartItems, Map<Long, Product> productMap) {
        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setSubtotalAmount(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItem.setCreateDatetime(LocalDateTime.now());
            orderItem.setCreateUser(SYSTEM_USER);

            orderItemRepository.save(orderItem);
        }
    }

    private void deductInventory(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            InventoryChangeRequest change = new InventoryChangeRequest();
            change.setProductId(item.getProductId());
            change.setQuantity(item.getQuantity());
            change.setOperator("order-service");

            ApiResponse<Boolean> deductRes = inventoryFeignClient.deductInventory(change);
            if (!deductRes.isSuccess() || Boolean.FALSE.equals(deductRes.getData())) {
                throw new RuntimeException("库存扣减失败，商品ID: " + item.getProductId());
            }
        }
    }

//    //处理人脸识别(从购物车下单)
//    private void overrideUserIdIfFaceRecognitionEnabled(CreateOrderFromCartRequest request) {
//        if (Boolean.TRUE.equals(request.getUseFaceRecognition())) {
//            String faceImagePath = request.getFaceImagePath();
//            if (faceImagePath == null || faceImagePath.isBlank()) {
//                throw new RuntimeException("用户启用了人脸识别，但未提供人脸图像路径");
//            }
//
//            ApiResponse<Long> faceRes = paymentFeignClient.verifyFace(faceImagePath);
//            if (!faceRes.isSuccess() || faceRes.getData() == null) {
//                throw new RuntimeException("人脸识别失败，无法识别用户身份");
//            }
//
//            request.setUserId(faceRes.getData());
//        }
//    }


    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public boolean updateOrder(Order order) {
        if (orderRepository.existsById(order.getOrderId())) {
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteOrder(Long orderId) {
        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
            return true;
        }
        return false;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> filterOrders(String status, Date startDate, Date endDate, Double minAmount, Double maxAmount) {
        List<Order> all = orderRepository.findAll();
        List<Order> filtered = new ArrayList<>();

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
