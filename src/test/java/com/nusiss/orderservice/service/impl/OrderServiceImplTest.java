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
import com.nusiss.orderservice.entity.Order;
import com.nusiss.orderservice.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;


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
    private PaymentFeignClient paymentFeignClient;

    @Mock
    private ShoppingCartFeignClient shoppingCartFeignClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        // 初始化mock对象
        MockitoAnnotations.openMocks(this);
    }

    /*
     测试用例：TC01 - 正常流程下单成功
     */
    @Test
    void testCreateDirectOrder_success() {
        // 准备测试数据
        DirectOrderRequest request = new DirectOrderRequest();
        request.setUserId(1L);
        request.setProductId(100L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");
        request.setPaymentMethod("WeChat");

        // 模拟商品信息
        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));

        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));
        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(10));

        // 模拟支付成功
        Payment mockPayment = new Payment();
        mockPayment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any(PaymentRequest.class))).thenReturn(ApiResponse.success(mockPayment));

        // 模拟库存扣减成功
        when(inventoryFeignClient.deductInventory(any(InventoryChangeRequest.class))).thenReturn(ApiResponse.success(true));

        // 模拟订单保存
        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setUserId(1L);
        savedOrder.setOrderStatus("CREATED");
        savedOrder.setPaymentStatus("UNPAID");
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setShippingAddress("Test Address");
        savedOrder.setCreateDatetime(LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // 模拟订单项保存
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());

        // 执行测试
        Order result = orderService.createDirectOrder(request);

        // 验证结果
        assertNotNull(result);
        assertEquals("PAID", result.getPaymentStatus());
        verify(orderRepository, times(2)).save(any(Order.class)); // 创建订单 + 更新订单
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    /*
     测试用例：TC02 - 商品不存在或无法获取
     */
    @Test
    void testCreateDirectOrder_productNotFound() {
        // 准备测试数据
        DirectOrderRequest request = new DirectOrderRequest();
        request.setUserId(1L);
        request.setProductId(100L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");

        // 模拟商品查询失败
        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.fail("Product not found"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createDirectOrder(request));
        assertEquals("商品不存在或无法获取商品信息", exception.getMessage());
    }

    /*
     测试用例：TC03 - 库存不足
     */
    @Test
    void testCreateDirectOrder_insufficientStock() {
        // 准备测试数据
        DirectOrderRequest request = new DirectOrderRequest();
        request.setUserId(1L);
        request.setProductId(100L);
        request.setQuantity(5);
        request.setShippingAddress("Test Address");

        // 模拟商品信息
        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));

        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));
        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(3));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createDirectOrder(request));
        assertEquals("库存不足，无法下单", exception.getMessage());
    }

    /*
     测试用例：TC04 - 支付失败
     */
    @Test
    void testCreateDirectOrder_paymentFailed() {
        // 准备测试数据
        DirectOrderRequest request = new DirectOrderRequest();
        request.setUserId(1L);
        request.setProductId(100L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");
        request.setPaymentMethod("Alipay");

        // 模拟商品信息
        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));

        // 模拟库存信息
        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(10));

        // ✅ 关键修复：Mock 保存订单返回非 null 的对象（避免 NPE）
        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setUserId(1L);
        savedOrder.setOrderStatus("CREATED");
        savedOrder.setPaymentStatus("UNPAID");
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setShippingAddress("Test Address");
        savedOrder.setCreateDatetime(LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // 模拟支付失败
        when(paymentFeignClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ApiResponse.error("Payment failed"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createDirectOrder(request));

        assertEquals("支付失败，订单未创建", exception.getMessage());
    }


    /*
     测试用例：TC05 - 扣减库存失败
     */
    @Test
    void testCreateDirectOrder_deductInventoryFailed() {
        // 准备测试数据
        DirectOrderRequest request = new DirectOrderRequest();
        request.setUserId(1L);
        request.setProductId(100L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");
        request.setPaymentMethod("WeChat");

        // 模拟商品信息
        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));

        // 模拟库存足够
        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(10));

        // 模拟保存订单（返回非空对象，避免空指针）
        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setUserId(1L);
        savedOrder.setOrderStatus("CREATED");
        savedOrder.setPaymentStatus("UNPAID");
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setShippingAddress("Test Address");
        savedOrder.setCreateDatetime(LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // 模拟支付成功
        Payment mockPayment = new Payment();
        mockPayment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any(PaymentRequest.class))).thenReturn(ApiResponse.success(mockPayment));

        // 模拟库存扣减失败
        when(inventoryFeignClient.deductInventory(any(InventoryChangeRequest.class))).thenReturn(ApiResponse.fail("库存扣减失败"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createDirectOrder(request));

        assertEquals("扣减库存失败", exception.getMessage());
    }

    @Test
    void testCreateOrderFromCart_Success() {
        Long userId = 1L;
        Long productId = 101L;
        int quantity = 2;

        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setShippingAddress("NUS Campus");
        request.setPaymentMethod("WeChat");

        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);

        Product product = new Product();
        product.setId(productId);
        product.setName("Phone");
        product.setPrice(BigDecimal.valueOf(100));

        Order order = new Order();
        order.setOrderId(999L);
        order.setUserId(userId);
        order.setPaymentStatus("UNPAID");
        order.setOrderStatus("CREATED");
        order.setTotalAmount(BigDecimal.valueOf(200));

        Payment payment = new Payment();
        payment.setPaymentStatus("PAID");

        when(shoppingCartFeignClient.getCartItems(userId))
                .thenReturn(ApiResponse.success(List.of(item)));
        when(inventoryFeignClient.getInventoryQuantity(productId))
                .thenReturn(ApiResponse.success(10));
        when(productFeignClient.getProductById(productId))
                .thenReturn(ApiResponse.success(product));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(paymentFeignClient.processPayment(any()))
                .thenReturn(ApiResponse.success(payment));
        when(inventoryFeignClient.deductInventory(any()))
                .thenReturn(ApiResponse.success(true));

        Order result = orderService.createOrderFromCart(request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(orderRepository, times(2)).save(any());
        verify(orderItemRepository).save(any());
        verify(shoppingCartFeignClient).clearCart(userId);
    }

    @Test
    void testCreateOrderFromCart_EmptyCart() {
        Long userId = 1L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);

        when(shoppingCartFeignClient.getCartItems(userId))
                .thenReturn(ApiResponse.success(List.of()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrderFromCart(request));
        assertTrue(ex.getMessage().contains("购物车为空"));
    }

    @Test
    void testCreateOrderFromCart_InsufficientStock() {
        Long userId = 1L;
        Long productId = 101L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);

        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(5);

        when(shoppingCartFeignClient.getCartItems(userId))
                .thenReturn(ApiResponse.success(List.of(item)));
        when(inventoryFeignClient.getInventoryQuantity(productId))
                .thenReturn(ApiResponse.success(1));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrderFromCart(request));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    void testCreateOrderFromCart_ProductInfoMissing() {
        Long userId = 1L;
        Long productId = 101L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);

        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(2);

        when(shoppingCartFeignClient.getCartItems(userId))
                .thenReturn(ApiResponse.success(List.of(item)));
        when(inventoryFeignClient.getInventoryQuantity(productId))
                .thenReturn(ApiResponse.success(10));
        when(productFeignClient.getProductById(productId))
                .thenReturn(ApiResponse.fail("商品不存在"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrderFromCart(request));
        assertTrue(ex.getMessage().contains("获取商品信息失败"));
    }

    @Test
    void testCreateOrderFromCart_PaymentFailed() {
        Long userId = 1L;
        Long productId = 101L;

        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setPaymentMethod("WeChat");

        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(2);

        Product product = new Product();
        product.setId(productId);
        product.setName("Phone");
        product.setPrice(BigDecimal.valueOf(100));

        Order order = new Order();
        order.setOrderId(999L);
        order.setUserId(userId);
        order.setPaymentStatus("UNPAID");
        order.setOrderStatus("CREATED");
        order.setTotalAmount(BigDecimal.valueOf(200));

        when(shoppingCartFeignClient.getCartItems(userId))
                .thenReturn(ApiResponse.success(List.of(item)));
        when(inventoryFeignClient.getInventoryQuantity(productId))
                .thenReturn(ApiResponse.success(10));
        when(productFeignClient.getProductById(productId))
                .thenReturn(ApiResponse.success(product));
        when(orderRepository.save(any())).thenReturn(order);
        when(paymentFeignClient.processPayment(any()))
                .thenReturn(ApiResponse.fail("支付失败"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrderFromCart(request));
        assertTrue(ex.getMessage().contains("支付失败"));
        verify(orderRepository).deleteById(order.getOrderId());
    }

    @Test
    void testCreateOrderFromCart_InventoryDeductFailed() {
        Long userId = 1L;
        Long productId = 101L;

        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setPaymentMethod("WeChat");

        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(2);

        Product product = new Product();
        product.setId(productId);
        product.setName("Phone");
        product.setPrice(BigDecimal.valueOf(100));

        Order order = new Order();
        order.setOrderId(999L);
        order.setUserId(userId);
        order.setPaymentStatus("UNPAID");
        order.setOrderStatus("CREATED");
        order.setTotalAmount(BigDecimal.valueOf(200));

        Payment payment = new Payment();
        payment.setPaymentStatus("PAID");

        when(shoppingCartFeignClient.getCartItems(userId))
                .thenReturn(ApiResponse.success(List.of(item)));
        when(inventoryFeignClient.getInventoryQuantity(productId))
                .thenReturn(ApiResponse.success(10));
        when(productFeignClient.getProductById(productId))
                .thenReturn(ApiResponse.success(product));
        when(orderRepository.save(any())).thenReturn(order);
        when(paymentFeignClient.processPayment(any()))
                .thenReturn(ApiResponse.success(payment));
        when(inventoryFeignClient.deductInventory(any()))
                .thenReturn(ApiResponse.success(false));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrderFromCart(request));
        assertTrue(ex.getMessage().contains("库存扣减失败"));
    }

    @Test
    void testFilterOrders_byStatus() {
        // Arrange
        Order order1 = new Order();
        order1.setOrderId(1L);
        order1.setOrderStatus("CREATED");
        order1.setOrderDate(LocalDateTime.now());
        order1.setTotalAmount(new BigDecimal("100.00"));

        Order order2 = new Order();
        order2.setOrderId(2L);
        order2.setOrderStatus("COMPLETED");
        order2.setOrderDate(LocalDateTime.now().plusDays(1));
        order2.setTotalAmount(new BigDecimal("200.00"));

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // Act
        List<Order> result = orderService.filterOrders("CREATED", null, null, null, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals("CREATED", result.get(0).getOrderStatus());
    }

    @Test
    void testGetOrdersWithPaginationAndSorting_asc() {
        // Arrange
        Order order1 = new Order();
        order1.setOrderId(1L);
        order1.setOrderStatus("CREATED");
        order1.setOrderDate(LocalDateTime.now());
        order1.setTotalAmount(new BigDecimal("100.00"));

        Order order2 = new Order();
        order2.setOrderId(2L);
        order2.setOrderStatus("COMPLETED");
        order2.setOrderDate(LocalDateTime.now().plusDays(1));
        order2.setTotalAmount(new BigDecimal("200.00"));

        Page<Order> page = new PageImpl<>(Arrays.asList(order1, order2));
        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        List<Order> result = orderService.getOrdersWithPaginationAndSorting(1, 10, "orderId", "asc");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).getOrderId() < result.get(1).getOrderId());
    }

    @Test
    void testGetOrdersWithPaginationAndSorting_desc() {

        // Arrange
        Order order1 = new Order();
        order1.setOrderId(1L);
        order1.setOrderStatus("CREATED");
        order1.setOrderDate(LocalDateTime.now());
        order1.setTotalAmount(new BigDecimal("100.00"));

        Order order2 = new Order();
        order2.setOrderId(2L);
        order2.setOrderStatus("COMPLETED");
        order2.setOrderDate(LocalDateTime.now().plusDays(1));
        order2.setTotalAmount(new BigDecimal("200.00"));

        List<Order> allOrders = Arrays.asList(order2, order1); // 手动按 desc 排序
        Page<Order> page = new PageImpl<>(allOrders);
        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        List<Order> result = orderService.getOrdersWithPaginationAndSorting(1, 10, "orderId", "desc");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).getOrderId() > result.get(1).getOrderId());
    }

}
