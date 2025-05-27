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
import org.springframework.data.domain.Sort;

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
        MockitoAnnotations.openMocks(this);
    }

    private Order createOrder(Long orderId, String status) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderStatus(status);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(new BigDecimal("100.00"));
        return order;
    }

    @Test
    void testCreateDirectOrder_success() {
        // Arrange
        DirectOrderRequest request = new DirectOrderRequest();
        request.setUserId(1L);
        request.setProductId(100L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));

        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));
        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(10));

        Payment mockPayment = new Payment();
        mockPayment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any(PaymentRequest.class))).thenReturn(ApiResponse.success(mockPayment));
        when(inventoryFeignClient.deductInventory(any(InventoryChangeRequest.class))).thenReturn(ApiResponse.success(true));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());

        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setUserId(1L);
        savedOrder.setOrderStatus("CREATED");
        savedOrder.setPaymentStatus("UNPAID");
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setShippingAddress("Test Address");
        savedOrder.setCreateDatetime(LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createDirectOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("PAID", result.getPaymentStatus());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void testCreateDirectOrder_productNotFound() {
        // Arrange
        DirectOrderRequest request = new DirectOrderRequest();
        request.setUserId(1L);
        request.setProductId(100L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");

        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.fail("Product not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createDirectOrder(request));

        assertEquals("商品不存在或无法获取商品信息", exception.getMessage());
    }

    @Test
    void testCreateDirectOrder_insufficientStock() {
        // Arrange
        DirectOrderRequest request = new DirectOrderRequest();
        request.setUserId(1L);
        request.setProductId(100L);
        request.setQuantity(2);
        request.setShippingAddress("Test Address");

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));

        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));
        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(1));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createDirectOrder(request));

        assertEquals("库存不足，无法下单", exception.getMessage());
    }

    @Test
    void testCreateOrderFromCart_success() {
        // Arrange
        Long userId = 1L;
        String shippingAddress = "Test Address";
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setShippingAddress(shippingAddress);

        CartItem cartItem = new CartItem();
        cartItem.setProductId(100L);
        cartItem.setQuantity(2);

        List<CartItem> cartItems = Collections.singletonList(cartItem);

        when(shoppingCartFeignClient.getCartItems(userId)).thenReturn(ApiResponse.success(cartItems));

        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(10));

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));

        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setUserId(userId);
        savedOrder.setOrderStatus("CREATED");
        savedOrder.setPaymentStatus("UNPAID");
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setShippingAddress(shippingAddress);
        savedOrder.setCreateDatetime(LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Payment mockPayment = new Payment();
        mockPayment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any(PaymentRequest.class))).thenReturn(ApiResponse.success(mockPayment));

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());

        when(inventoryFeignClient.deductInventory(any(InventoryChangeRequest.class))).thenReturn(ApiResponse.success(true));

        when(shoppingCartFeignClient.clearCart(100L)).thenReturn(ApiResponse.success()); // 模拟成功清空购物车

        // Act
        Order result = orderService.createOrderFromCart(request);

        // Assert
        assertNotNull(result);
        assertEquals("PAID", result.getPaymentStatus());
        verify(shoppingCartFeignClient).getCartItems(userId);
        verify(inventoryFeignClient).getInventoryQuantity(100L);
        verify(productFeignClient).getProductById(100L);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(inventoryFeignClient).deductInventory(any(InventoryChangeRequest.class));
        verify(shoppingCartFeignClient).clearCart(userId);
    }

    @Test
    void testCreateOrderFromCart_emptyCart() {
        // Arrange
        Long userId = 1L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setShippingAddress("Test Address");

        when(shoppingCartFeignClient.getCartItems(userId)).thenReturn(ApiResponse.fail("Cart is empty"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createOrderFromCart(request));

        assertEquals("购物车为空，无法下单", exception.getMessage());
        verify(shoppingCartFeignClient).getCartItems(userId);
    }

    @Test
    void testCreateOrderFromCart_insufficientInventory() {
        // Arrange
        Long userId = 1L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setShippingAddress("Test Address");

        CartItem cartItem = new CartItem();
        cartItem.setProductId(100L);
        cartItem.setQuantity(2);

        List<CartItem> cartItems = Collections.singletonList(cartItem);
        when(shoppingCartFeignClient.getCartItems(userId)).thenReturn(ApiResponse.success(cartItems));

        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(1));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createOrderFromCart(request));

        assertEquals("商品库存不足，商品ID: 100", exception.getMessage());
        verify(shoppingCartFeignClient).getCartItems(userId);
        verify(inventoryFeignClient).getInventoryQuantity(100L);
    }

    @Test
    void testCreateOrderFromCart_getInventoryFailed() {
        // Arrange
        Long userId = 1L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setShippingAddress("Test Address");

        CartItem cartItem = new CartItem();
        cartItem.setProductId(100L);
        cartItem.setQuantity(2);

        List<CartItem> cartItems = Collections.singletonList(cartItem);
        when(shoppingCartFeignClient.getCartItems(userId)).thenReturn(ApiResponse.success(cartItems));

        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.error("获取库存失败"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createOrderFromCart(request));

        assertEquals("无法获取商品库存，商品ID: 100", exception.getMessage());
        verify(shoppingCartFeignClient).getCartItems(userId);
        verify(inventoryFeignClient).getInventoryQuantity(100L);
    }

    @Test
    void testCreateOrderFromCart_getProductFailed() {
        // Arrange
        Long userId = 1L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setShippingAddress("Test Address");

        CartItem cartItem = new CartItem();
        cartItem.setProductId(100L);
        cartItem.setQuantity(2);

        List<CartItem> cartItems = Collections.singletonList(cartItem);
        when(shoppingCartFeignClient.getCartItems(userId)).thenReturn(ApiResponse.success(cartItems));

        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(10));

        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.error("获取商品失败"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createOrderFromCart(request));

        assertEquals("获取商品信息失败，商品ID: 100", exception.getMessage());
        verify(shoppingCartFeignClient).getCartItems(userId);
        verify(inventoryFeignClient).getInventoryQuantity(100L);
        verify(productFeignClient).getProductById(100L);
    }

    @Test
    void testCreateOrderFromCart_paymentFailed() {
        // Arrange
        Long userId = 1L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setShippingAddress("Test Address");

        CartItem cartItem = new CartItem();
        cartItem.setProductId(100L);
        cartItem.setQuantity(2);

        List<CartItem> cartItems = Collections.singletonList(cartItem);
        when(shoppingCartFeignClient.getCartItems(userId)).thenReturn(ApiResponse.success(cartItems));

        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(10));

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));

        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setUserId(userId);
        savedOrder.setOrderStatus("CREATED");
        savedOrder.setPaymentStatus("UNPAID");
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setShippingAddress("Test Address");
        savedOrder.setCreateDatetime(LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        when(paymentFeignClient.processPayment(any(PaymentRequest.class))).thenReturn(ApiResponse.fail("支付失败"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createOrderFromCart(request));

        assertEquals("支付失败，订单未完成", exception.getMessage());
        verify(shoppingCartFeignClient).getCartItems(userId);
        verify(inventoryFeignClient).getInventoryQuantity(100L);
        verify(productFeignClient).getProductById(100L);
        verify(orderRepository).save(any(Order.class));
        verify(paymentFeignClient).processPayment(any(PaymentRequest.class));
    }

    @Test
    void testCreateOrderFromCart_deductInventoryFailed() {
        // Arrange
        Long userId = 1L;
        CreateOrderFromCartRequest request = new CreateOrderFromCartRequest();
        request.setUserId(userId);
        request.setShippingAddress("Test Address");

        CartItem cartItem = new CartItem();
        cartItem.setProductId(100L);
        cartItem.setQuantity(2);

        List<CartItem> cartItems = Collections.singletonList(cartItem);
        when(shoppingCartFeignClient.getCartItems(userId)).thenReturn(ApiResponse.success(cartItems));

        when(inventoryFeignClient.getInventoryQuantity(100L)).thenReturn(ApiResponse.success(10));

        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        when(productFeignClient.getProductById(100L)).thenReturn(ApiResponse.success(product));

        Order savedOrder = new Order();
        savedOrder.setOrderId(1L);
        savedOrder.setUserId(userId);
        savedOrder.setOrderStatus("CREATED");
        savedOrder.setPaymentStatus("UNPAID");
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setShippingAddress("Test Address");
        savedOrder.setCreateDatetime(LocalDateTime.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Payment mockPayment = new Payment();
        mockPayment.setPaymentStatus("PAID");
        when(paymentFeignClient.processPayment(any(PaymentRequest.class))).thenReturn(ApiResponse.success(mockPayment));

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());

        when(inventoryFeignClient.deductInventory(any(InventoryChangeRequest.class))).thenReturn(ApiResponse.success(false));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.createOrderFromCart(request));

        assertEquals("库存扣减失败，商品ID: 100", exception.getMessage());
        verify(shoppingCartFeignClient).getCartItems(userId);
        verify(inventoryFeignClient).getInventoryQuantity(100L);
        verify(productFeignClient).getProductById(100L);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(paymentFeignClient).processPayment(any(PaymentRequest.class));
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(inventoryFeignClient).deductInventory(any(InventoryChangeRequest.class));
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
        Order order1 = createOrder(1L, "CREATED");
        Order order2 = createOrder(2L, "COMPLETED");

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
