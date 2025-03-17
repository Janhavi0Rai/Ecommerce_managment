package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.*;

import com.cleartrip.ecommerce_management_project.repository.OrderRepository;
import com.cleartrip.ecommerce_management_project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;



    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;
    private Cart testCart;
    private CartItem testCartItem;
    private OrderController.OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        testProduct.setCategory("Electronics");

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(99.99);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setItems(new ArrayList<>(Arrays.asList(testOrderItem)));
        testOrder.setTotalAmount(199.98); // 2 * 99.99
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setShippingAddress("123 Test St");
        testOrder.setPaymentMethod("Credit Card");

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>(Arrays.asList(testCartItem)));
        testCart.setTotalPrice(199.98);

        orderRequest = new OrderController.OrderRequest();
        orderRequest.setShippingAddress("123 Test St");
        orderRequest.setPaymentMethod("Credit Card");
    }

    @Test
    void createOrder_ShouldCreateNewOrder() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
            OrderItem item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });
        when(cartService.clearCart(1L)).thenReturn(Optional.of(new Cart()));

        // When
        Order result = orderService.createOrder(1L, orderRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUser().getId()).isEqualTo(1L);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalAmount()).isEqualTo(199.98);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getShippingAddress()).isEqualTo("123 Test St");
        assertThat(result.getPaymentMethod()).isEqualTo("Credit Card");
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        Optional<Order> result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getTotalAmount()).isEqualTo(199.98);
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderService.getOrderById(99L);

        // Then
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findById(99L);
    }

    @Test
    void getUserOrders_ShouldReturnUserOrders() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders);
        when(orderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(orderPage);

        // When
        Page<Order> result = orderService.getUserOrders(1L, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(orderRepository, times(1)).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() {
        // Given
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setStatus(OrderStatus.SHIPPED);
        updatedOrder.setUser(testUser);
        updatedOrder.setItems(testOrder.getItems());
        updatedOrder.setTotalAmount(testOrder.getTotalAmount());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        // When
        Optional<Order> result = orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderService.updateOrderStatus(99L, OrderStatus.SHIPPED);

        // Then
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findById(99L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_ShouldCancelOrder() {
        // Given
        Order cancelledOrder = new Order();
        cancelledOrder.setId(1L);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        cancelledOrder.setUser(testUser);
        cancelledOrder.setItems(testOrder.getItems());
        cancelledOrder.setTotalAmount(testOrder.getTotalAmount());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

        // When
        Optional<Order> result = orderService.cancelOrder(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderService.cancelOrder(99L);

        // Then
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findById(99L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenOrderAlreadyDelivered_ShouldNotCancel() {
        // Given
        Order deliveredOrder = new Order();
        deliveredOrder.setId(1L);
        deliveredOrder.setStatus(OrderStatus.DELIVERED);
        deliveredOrder.setUser(testUser);
        deliveredOrder.setItems(testOrder.getItems());
        deliveredOrder.setTotalAmount(testOrder.getTotalAmount());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

        // When
        Optional<Order> result = orderService.cancelOrder(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.DELIVERED); // Status unchanged
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders);
        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(orderPage);

        // When
        Page<Order> result = orderService.getAllOrders(0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(orderRepository, times(1)).findAll(any(PageRequest.class));
    }

    // Helper class for order creation
    public static class OrderController {
        public static class OrderRequest {
            private String shippingAddress;
            private String paymentMethod;

            public String getShippingAddress() {
                return shippingAddress;
            }

            public void setShippingAddress(String shippingAddress) {
                this.shippingAddress = shippingAddress;
            }

            public String getPaymentMethod() {
                return paymentMethod;
            }

            public void setPaymentMethod(String paymentMethod) {
                this.paymentMethod = paymentMethod;
            }
        }
    }
} 