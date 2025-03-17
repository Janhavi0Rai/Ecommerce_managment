package com.cleartrip.ecommerce_management_project.controller;

import com.cleartrip.ecommerce_management_project.model.Cart;
import com.cleartrip.ecommerce_management_project.model.CartItem;
import com.cleartrip.ecommerce_management_project.model.Product;
import com.cleartrip.ecommerce_management_project.model.User;
import com.cleartrip.ecommerce_management_project.service.CartService;
import com.cleartrip.ecommerce_management_project.service.ProductService;
import com.cleartrip.ecommerce_management_project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private UserService userService;

    @MockBean
    private ProductService productService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

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

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>(Arrays.asList(testCartItem)));
        testCart.setTotalPrice(199.98); // 2 * 99.99
    }

    @Test
    void getCart_ShouldReturnCart() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(Optional.of(testCart));

        mockMvc.perform(get("/api/cart")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.totalPrice", is(199.98)))
                .andExpect(jsonPath("$.items", hasSize(1)));
        
        verify(cartService, times(1)).getCartByUserId(1L);
    }

    @Test
    void addToCart_ShouldAddItemToCart() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        when(cartService.addItemToCart(eq(1L), eq(1L), eq(2))).thenReturn(testCart);

        mockMvc.perform(post("/api/cart/add")
                .param("userId", "1")
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice", is(199.98)))
                .andExpect(jsonPath("$.items", hasSize(1)));
        
        verify(cartService, times(1)).addItemToCart(1L, 1L, 2);
    }

    @Test
    void updateCartItem_ShouldUpdateItemQuantity() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.updateCartItemQuantity(eq(1L), eq(1L), eq(3))).thenReturn(Optional.of(testCart));

        mockMvc.perform(put("/api/cart/update")
                .param("userId", "1")
                .param("itemId", "1")
                .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));
        
        verify(cartService, times(1)).updateCartItemQuantity(1L, 1L, 3);
    }

    @Test
    void removeFromCart_ShouldRemoveItemFromCart() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.removeItemFromCart(eq(1L), eq(1L))).thenReturn(Optional.of(testCart));

        mockMvc.perform(delete("/api/cart/remove")
                .param("userId", "1")
                .param("itemId", "1"))
                .andExpect(status().isOk());
        
        verify(cartService, times(1)).removeItemFromCart(1L, 1L);
    }

    @Test
    void clearCart_ShouldClearAllItems() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.clearCart(1L)).thenReturn(Optional.of(new Cart()));

        mockMvc.perform(delete("/api/cart/clear")
                .param("userId", "1"))
                .andExpect(status().isOk());
        
        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    void getCart_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cart")
                .param("userId", "99"))
                .andExpect(status().isNotFound());
        
        verify(cartService, never()).getCartByUserId(anyLong());
    }

    @Test
    void addToCart_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(productService.getProductById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/cart/add")
                .param("userId", "1")
                .param("productId", "99")
                .param("quantity", "2"))
                .andExpect(status().isNotFound());
        
        verify(cartService, never()).addItemToCart(anyLong(), anyLong(), anyInt());
    }
} 