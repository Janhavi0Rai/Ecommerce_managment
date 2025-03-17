package com.cleartrip.ecommerce_management_project.controller;

import com.cleartrip.ecommerce_management_project.model.*;
import com.cleartrip.ecommerce_management_project.service.CartService;
import com.cleartrip.ecommerce_management_project.service.OrderService;
import com.cleartrip.ecommerce_management_project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserService userService;

    @MockBean
    private CartService cartService;

    private User testUser;
    private Order testOrder;
    private Cart testCart;
    private Product testProduct;
    private OrderItem testOrderItem;

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

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(2);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>(Arrays.asList(cartItem)));
        testCart.setTotalPrice(199.98);
    }

    @Test
    void createOrder_ShouldCreateNewOrder() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddress("123 Test St");
        orderRequest.setPaymentMethod("Credit Card");

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(Optional.of(testCart));
        when(orderService.createOrder(eq(1L), any(OrderRequest.class))).thenReturn(testOrder);

        mockMvc.perform(post("/api/orders")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.totalAmount", is(199.98)))
                .andExpect(jsonPath("$.status", is("PENDING")));
        
        verify(orderService, times(1)).createOrder(eq(1L), any(OrderRequest.class));
    }

    @Test
    void getOrderById_ShouldReturnOrder() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

        mockMvc.perform(get("/api/orders/1")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.totalAmount", is(199.98)));
        
        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void getUserOrders_ShouldReturnUserOrders() throws Exception {
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders);
        
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(orderService.getUserOrders(eq(1L), anyInt(), anyInt())).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders/user")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)));
        
        verify(orderService, times(1)).getUserOrders(eq(1L), anyInt(), anyInt());
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.SHIPPED))).thenReturn(Optional.of(testOrder));

        mockMvc.perform(put("/api/orders/1/status")
                .param("userId", "1")
                .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING"))); // The mock returns the original status
        
        verify(orderService, times(1)).updateOrderStatus(1L, OrderStatus.SHIPPED);
    }

    @Test
    void cancelOrder_ShouldCancelOrder() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(orderService.cancelOrder(1L)).thenReturn(Optional.of(testOrder));

        mockMvc.perform(put("/api/orders/1/cancel")
                .param("userId", "1"))
                .andExpect(status().isOk());
        
        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    void createOrder_WhenCartEmpty_ShouldReturnBadRequest() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        Cart emptyCart = new Cart();
        emptyCart.setItems(new ArrayList<>());
        
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(Optional.of(emptyCart));

        mockMvc.perform(post("/api/orders")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
        
        verify(orderService, never()).createOrder(anyLong(), any(OrderRequest.class));
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/99")
                .param("userId", "1"))
                .andExpect(status().isNotFound());
    }

    // Helper class for order creation
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