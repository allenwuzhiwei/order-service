package com.nusiss.orderservice.service.impl;

import com.nusiss.commonservice.entity.*;
import com.nusiss.commonservice.feign.InventoryFeignClient;
import com.nusiss.commonservice.feign.PaymentFeignClient;
import com.nusiss.commonservice.feign.ProductFeignClient;
import com.nusiss.commonservice.feign.ShoppingCartFeignClient;
import com.nusiss.commonservice.config.ApiResponse;
import com.nusiss.orderservice.dao.OrderItemRepository;
import com.nusiss.orderservice.dao.OrderRepository;
import com.nusiss.orderservice.dto.CreateOrderFromCartRequest;
import com.nusiss.orderservice.dto.DirectOrderRequest;
import com.nusiss.orderservice.dto.FacePaymentDirectOrderRequest;
import com.nusiss.orderservice.entity.Order;
import com.nusiss.orderservice.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private ProductFeignClient productFeignClient;

    @Mock
    private InventoryFeignClient inventoryFeignClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentFeignClient paymentFeignClient;

    @Mock
    private ShoppingCartFeignClient shoppingCartFeignClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateDirectOrder_success() {
        // 1. 准备请求参数
        DirectOrderRequest request = new DirectOrderRequest();
        request.setProductId(1L);
        request.setQuantity(2);
        request.setUserId(100L);
        request.setShippingAddress("Test Address");
        request.setPaymentMethod("WeChat");

        // 2. 准备 Product
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("50.00"));
        product.setSellerId(999L);

        when(productFeignClient.getProductById(1L))
                .thenReturn(ApiResponse.success(product));

        // 3. 库存充足
        when(inventoryFeignClient.getInventoryQuantity(1L))
                .thenReturn(ApiResponse.success(10));

        // 4. 保存订单时返回带有 ID 的订单
        Order savedOrder = new Order();
        savedOrder.setOrderId(123L);
        savedOrder.setTotalAmount(new BigDecimal("100.00"));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        // 5. 支付成功
        Payment payment = new Payment();
        payment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any()))
                .thenReturn(ApiResponse.success(payment));

        // 6. 扣减库存成功
        when(inventoryFeignClient.deductInventory(any()))
                .thenReturn(ApiResponse.success(true));

        // 7. 保存订单项
        when(orderItemRepository.save(any(OrderItem.class)))
                .thenReturn(new OrderItem());

        // === 测试执行 ===
        Order result = orderService.createDirectOrder(request);

        // === 断言 ===
        assertNotNull(result);
        assertEquals("PAID", result.getPaymentStatus());

        // === 验证 ===
        verify(orderRepository, times(2)).save(any()); // 创建订单 + 更新支付状态
        verify(orderItemRepository).save(any());
        verify(inventoryFeignClient).deductInventory(any());
    }

    @Test
    void testCreateOrderWithFaceRecognition_success() throws Exception {
        // 准备请求对象和人脸图片
        FacePaymentDirectOrderRequest request = new FacePaymentDirectOrderRequest();
        request.setProductId(1L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");
        request.setPaymentMethod("FaceRecognition");

        // 模拟 MultipartFile
        MockMultipartFile faceImage = new MockMultipartFile("file", "face.jpg", "image/jpeg", new byte[]{1, 2, 3});

        // 模拟人脸识别返回结果
        String faceApiResponse = """
        {
            "status": "200",
            "message": "success",
            "userId": "100"
        }
        """;
        ResponseEntity<String> response = ResponseEntity.ok(faceApiResponse);
        when(paymentFeignClient.verifyFace(faceImage)).thenReturn(response);

        // 重用前一个测试中所有必要的 mock（商品、库存、订单保存、支付、扣库存）
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("50.00"));
        product.setSellerId(999L);

        when(productFeignClient.getProductById(1L)).thenReturn(ApiResponse.success(product));
        when(inventoryFeignClient.getInventoryQuantity(1L)).thenReturn(ApiResponse.success(10));
        Order savedOrder = new Order();
        savedOrder.setOrderId(123L);
        savedOrder.setTotalAmount(new BigDecimal("100.00"));
        when(orderRepository.save(any())).thenReturn(savedOrder);

        Payment payment = new Payment();
        payment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any())).thenReturn(ApiResponse.success(payment));
        when(inventoryFeignClient.deductInventory(any())).thenReturn(ApiResponse.success(true));
        when(orderItemRepository.save(any())).thenReturn(new OrderItem());

        // === 测试执行 ===
        Order result = orderService.createOrderWithFaceRecognition(request, faceImage);

        // === 断言 ===
        assertNotNull(result);
        assertEquals("PAID", result.getPaymentStatus());

        // === 验证 ===
        verify(paymentFeignClient).verifyFace(faceImage);
        verify(orderRepository, times(2)).save(any()); // 创建订单 & 更新状态
    }

    @Test
    void testCreateOrderFromCart_success() {
        // 准备请求
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(100L);
        request.setShippingAddress("Test Address");
        request.setPaymentMethod("WeChat");

        // 模拟购物车项
        CartItem cartItem = new CartItem();
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        List<CartItem> cartItems = List.of(cartItem);

        when(shoppingCartFeignClient.getCartItems(100L)).thenReturn(ApiResponse.success(cartItems));

        // 模拟库存足够
        when(inventoryFeignClient.getInventoryQuantity(1L)).thenReturn(ApiResponse.success(10));

        // 模拟商品信息
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("50.00"));
        product.setSellerId(999L);
        when(productFeignClient.getProductById(1L)).thenReturn(ApiResponse.success(product));

        // 模拟保存订单
        Order savedOrder = new Order();
        savedOrder.setOrderId(200L);
        savedOrder.setTotalAmount(new BigDecimal("100.00"));
        when(orderRepository.save(any())).thenReturn(savedOrder);

        // 模拟支付成功
        Payment payment = new Payment();
        payment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any())).thenReturn(ApiResponse.success(payment));

        // 模拟扣减库存成功
        when(inventoryFeignClient.deductInventory(any())).thenReturn(ApiResponse.success(true));

        // 模拟保存订单项
        when(orderItemRepository.save(any())).thenReturn(new OrderItem());

        // 测试执行
        Order result = orderService.createOrderFromCart(request);

        // 断言
        assertNotNull(result);
        assertEquals("PAID", result.getPaymentStatus());

        // 验证调用
        verify(shoppingCartFeignClient).getCartItems(100L);
        verify(orderRepository, times(2)).save(any());
        verify(paymentFeignClient).processPayment(any());
        verify(inventoryFeignClient).deductInventory(any());
        verify(shoppingCartFeignClient).clearCart(100L);
    }

    @Test
    void testCreateOrderFromCartWithFaceRecognition_success() throws Exception {
        // Step 1: 构造请求对象 + face image
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setShippingAddress("Test Address");
        request.setPaymentMethod("WeChat");

        MockMultipartFile faceImage = new MockMultipartFile("file", "face.jpg", "image/jpeg", new byte[]{1, 2, 3});

        // Step 2: 模拟人脸识别服务返回 userId
        String faceResponseJson = """
        {
            "status": "200",
            "message": "success",
            "userId": "100"
        }
        """;
        ResponseEntity<String> faceRes = ResponseEntity.ok(faceResponseJson);
        when(paymentFeignClient.verifyFace(faceImage)).thenReturn(faceRes);

        // Step 3: 模拟购物车、库存、商品、支付、扣库存（跟上一个方法一样）

        CartItem cartItem = new CartItem();
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        List<CartItem> cartItems = List.of(cartItem);
        when(shoppingCartFeignClient.getCartItems(100L)).thenReturn(ApiResponse.success(cartItems));
        when(inventoryFeignClient.getInventoryQuantity(1L)).thenReturn(ApiResponse.success(10));

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("50.00"));
        product.setSellerId(999L);
        when(productFeignClient.getProductById(1L)).thenReturn(ApiResponse.success(product));

        Order savedOrder = new Order();
        savedOrder.setOrderId(300L);
        savedOrder.setTotalAmount(new BigDecimal("100.00"));
        when(orderRepository.save(any())).thenReturn(savedOrder);

        Payment payment = new Payment();
        payment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any())).thenReturn(ApiResponse.success(payment));
        when(inventoryFeignClient.deductInventory(any())).thenReturn(ApiResponse.success(true));
        when(orderItemRepository.save(any())).thenReturn(new OrderItem());

        // Step 4: 执行测试方法
        Order result = orderService.createOrderFromCartWithFaceRecognition(request, faceImage);

        // Step 5: 验证
        assertNotNull(result);
        assertEquals("PAID", result.getPaymentStatus());

        verify(paymentFeignClient).verifyFace(faceImage);
        verify(shoppingCartFeignClient).clearCart(100L);
    }

    @Test
    void testGetOrderById_found() {
        Order order = new Order();
        order.setOrderId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getOrderId());
    }

    @Test
    void testGetOrderById_notFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(2L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllOrders() {
        List<Order> orders = List.of(new Order(), new Order());
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();
        assertEquals(2, result.size());
    }

    @Test
    void testUpdateOrder_success() {
        Order order = new Order();
        order.setOrderId(1L);

        when(orderRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.save(order)).thenReturn(order);

        boolean result = orderService.updateOrder(order);
        assertTrue(result);
    }

    @Test
    void testUpdateOrder_notExist() {
        Order order = new Order();
        order.setOrderId(2L);

        when(orderRepository.existsById(2L)).thenReturn(false);

        boolean result = orderService.updateOrder(order);
        assertFalse(result);
    }

    @Test
    void testDeleteOrder_success() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        boolean result = orderService.deleteOrder(1L);
        assertTrue(result);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void testDeleteOrder_notExist() {
        when(orderRepository.existsById(2L)).thenReturn(false);
        boolean result = orderService.deleteOrder(2L);
        assertFalse(result);
    }

    @Test
    void testGetOrdersByUserId() {
        List<Order> orders = List.of(new Order(), new Order());
        when(orderRepository.findByUserId(100L)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByUserId(100L);
        assertEquals(2, result.size());
    }

    @Test
    void testFilterOrders_withStatusAndAmountRange() {
        Order order1 = new Order();
        order1.setOrderStatus("CREATED");
        order1.setOrderDate(LocalDateTime.now());
        order1.setTotalAmount(new BigDecimal("100.00"));

        Order order2 = new Order();
        order2.setOrderStatus("SHIPPED");
        order2.setOrderDate(LocalDateTime.now());
        order2.setTotalAmount(new BigDecimal("200.00"));

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        List<Order> result = orderService.filterOrders(
                "CREATED",
                null,
                null,
                50.0,
                150.0
        );

        assertEquals(1, result.size());
        assertEquals("CREATED", result.get(0).getOrderStatus());
    }

    @Test
    void testGetOrdersWithPaginationAndSorting() {
        Order order = new Order();
        order.setOrderId(1L);
        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<Order> result = orderService.getOrdersWithPaginationAndSorting(1, 10, "orderId", "asc");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getOrderId());
    }


}