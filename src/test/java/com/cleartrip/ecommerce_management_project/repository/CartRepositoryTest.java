package com.cleartrip.ecommerce_management_project.repository;

import com.cleartrip.ecommerce_management_project.model.Cart;
import com.cleartrip.ecommerce_management_project.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CartRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUserId_ShouldReturnCart() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = entityManager.persistAndFlush(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(0.0);
        cart = entityManager.persistAndFlush(cart);

        // When
        Optional<Cart> found = cartRepository.findByUserId(user.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserId_WhenNoCart_ShouldReturnEmpty() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = entityManager.persistAndFlush(user);

        // When
        Optional<Cart> found = cartRepository.findByUserId(user.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void deleteByUserId_ShouldDeleteCart() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = entityManager.persistAndFlush(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(0.0);
        cart = entityManager.persistAndFlush(cart);

        // When
        cartRepository.deleteByUserId(user.getId());
        entityManager.flush();

        // Then
        Optional<Cart> found = cartRepository.findByUserId(user.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldPersistCart() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = entityManager.persistAndFlush(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(99.99);

        // When
        Cart savedCart = cartRepository.save(cart);
        entityManager.flush();

        // Then
        Cart found = entityManager.find(Cart.class, savedCart.getId());
        assertThat(found).isNotNull();
        assertThat(found.getTotalPrice()).isEqualTo(99.99);
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
    }
} 