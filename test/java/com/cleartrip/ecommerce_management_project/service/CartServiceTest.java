package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.Cart;
import com.cleartrip.ecommerce_management_project.model.CartItem;
import com.cleartrip.ecommerce_management_project.model.Product;
import com.cleartrip.ecommerce_management_project.model.User;
import com.cleartrip.ecommerce_management_project.repository.CartItemRepository;
import com.cleartrip.ecommerce_management_project.repository.CartRepository;
import com.cleartrip.ecommerce_management_project.repository.ProductRepository;
import com.cleartrip.ecommerce_management_project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

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
    void getCartByUserId_ShouldReturnCart() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // When
        Optional<Cart> result = cartService.getCartByUserId(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getTotalPrice()).isEqualTo(199.98);
        verify(cartRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getCartByUserId_WhenCartDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        // When
        Optional<Cart> result = cartService.getCartByUserId(99L);

        // Then
        assertThat(result).isEmpty();
        verify(cartRepository, times(1)).findByUserId(99L);
    }

    @Test
    void addItemToCart_WhenCartExists_ShouldAddItem() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(createProduct(2L, "New Product", 49.99)));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(2L);
            return item;
        });

        // When
        Cart result = cartService.addItemToCart(1L, 2L, 3);

        // Then
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotalPrice()).isEqualTo(199.98 + (49.99 * 3)); // Original + new items
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(productRepository, times(1)).findById(2L);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItemToCart_WhenCartDoesNotExist_ShouldCreateCartAndAddItem() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(1L);
            return cart;
        });
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        // When
        Cart result = cartService.addItemToCart(1L, 1L, 2);

        // Then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalPrice()).isEqualTo(199.98); // 2 * 99.99
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(2)).save(any(Cart.class)); // Once for new cart, once for updated cart
    }

    @Test
    void addItemToCart_WhenItemAlreadyInCart_ShouldUpdateQuantity() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Cart result = cartService.addItemToCart(1L, 1L, 3);

        // Then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5); // 2 + 3
        assertThat(result.getTotalPrice()).isEqualTo(499.95); // 5 * 99.99
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(cartItemRepository, never()).save(any(CartItem.class)); // No new item saved
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_ShouldUpdateQuantity() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Cart> result = cartService.updateCartItemQuantity(1L, 1L, 5);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(result.get().getTotalPrice()).isEqualTo(499.95); // 5 * 99.99
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_WhenCartNotFound_ShouldReturnEmpty() {
        // Given
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        // When
        Optional<Cart> result = cartService.updateCartItemQuantity(99L, 1L, 5);

        // Then
        assertThat(result).isEmpty();
        verify(cartRepository, times(1)).findByUserId(99L);
        verify(cartItemRepository, never()).findById(anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_WhenItemNotFound_ShouldReturnOriginalCart() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Cart> result = cartService.updateCartItemQuantity(1L, 99L, 5);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getItems()).hasSize(1);
        assertThat(result.get().getItems().get(0).getQuantity()).isEqualTo(2); // Unchanged
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(cartItemRepository, times(1)).findById(99L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeItemFromCart_ShouldRemoveItem() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setItems(new ArrayList<>()); // Empty after removal
            cart.setTotalPrice(0.0);
            return cart;
        });

        // When
        Optional<Cart> result = cartService.removeItemFromCart(1L, 1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getItems()).isEmpty();
        assertThat(result.get().getTotalPrice()).isEqualTo(0.0);
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).delete(any(CartItem.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void removeItemFromCart_WhenCartNotFound_ShouldReturnEmpty() {
        // Given
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        // When
        Optional<Cart> result = cartService.removeItemFromCart(99L, 1L);

        // Then
        assertThat(result).isEmpty();
        verify(cartRepository, times(1)).findByUserId(99L);
        verify(cartItemRepository, never()).findById(anyLong());
        verify(cartItemRepository, never()).delete(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeItemFromCart_WhenItemNotFound_ShouldReturnOriginalCart() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Cart> result = cartService.removeItemFromCart(1L, 99L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getItems()).hasSize(1); // Unchanged
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(cartItemRepository, times(1)).findById(99L);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void clearCart_ShouldRemoveAllItems() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        doNothing().when(cartItemRepository).deleteAll(anyList());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setItems(new ArrayList<>());
            cart.setTotalPrice(0.0);
            return cart;
        });

        // When
        Optional<Cart> result = cartService.clearCart(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getItems()).isEmpty();
        assertThat(result.get().getTotalPrice()).isEqualTo(0.0);
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(cartItemRepository, times(1)).deleteAll(anyList());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void clearCart_WhenCartNotFound_ShouldReturnEmpty() {
        // Given
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        // When
        Optional<Cart> result = cartService.clearCart(99L);

        // Then
        assertThat(result).isEmpty();
        verify(cartRepository, times(1)).findByUserId(99L);
        verify(cartItemRepository, never()).deleteAll(anyList());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // Helper method to create products
    private Product createProduct(Long id, String name, double price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
} 