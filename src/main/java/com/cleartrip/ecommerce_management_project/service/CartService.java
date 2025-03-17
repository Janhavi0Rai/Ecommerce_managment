package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.*;
import com.cleartrip.ecommerce_management_project.repository.CartRepository;
import com.cleartrip.ecommerce_management_project.repository.ProductRepository;
import com.cleartrip.ecommerce_management_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<Cart> getOrCreateCart(User user) {
        Optional<Cart> existingCart = cartRepository.findByUserId(user.getId());
        if (existingCart.isPresent()) {
            return existingCart;
        } else {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setItems(new ArrayList<>());
            return Optional.of(cartRepository.save(newCart));
        }
    }

    public Optional<Cart> addToCart(User user, Product product, Integer quantity) {
        Optional<Cart> cartOptional = getOrCreateCart(user);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();

            Optional<Inventory> inventoryOptional = inventoryService.getInventoryByProduct(product);
            if (inventoryOptional.isEmpty() || inventoryOptional.get().getQuantity() < quantity) {
                return Optional.empty();
            }

            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                cart.getItems().add(newItem);
            }

            return Optional.of(cartRepository.save(cart));
        }
        return Optional.empty();
    }

    public Optional<Cart> removeFromCart(User user, Product product) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(user.getId());
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            cart.getItems().removeIf(item -> item.getProduct().getId().equals(product.getId()));
            return Optional.of(cartRepository.save(cart));
        }
        return Optional.empty();
    }

    public Optional<Cart> getCartByUser(User user) {
        return cartRepository.findByUserId(user.getId());
    }
    public Optional<Cart> getCartByUserId(long userId){return cartRepository.findByUserId(userId);};
    //
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Transactional
    public Cart addItemToCart(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(0.0);
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);

        return cartRepository.save(cart);
    }

    @Transactional
    public Optional<Cart> updateCartItemQuantity(Long userId, Long itemId, int quantity) {
        if (quantity <= 0) {
            return removeItemFromCart(userId, itemId);
        }

        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return Optional.empty();
        }

        Cart cart = cartOpt.get();
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (itemOpt.isEmpty()) {
            return Optional.of(cart);
        }

        CartItem item = itemOpt.get();
        item.setQuantity(quantity);

        cart.recalculateTotalPrice();
        cartRepository.save(cart);

        return Optional.of(cart);
    }

    @Transactional
    public Optional<Cart> removeItemFromCart(Long userId, Long itemId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return Optional.empty();
        }

        Cart cart = cartOpt.get();
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (itemOpt.isEmpty()) {
            return Optional.of(cart);
        }

        CartItem item = itemOpt.get();
        cart.getItems().remove(item);

        cart.recalculateTotalPrice();
        cartRepository.save(cart);

        return Optional.of(cart);
    }

    @Transactional
    public Optional<Cart> clearCart(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return Optional.empty();
        }

        Cart cart = cartOpt.get();
        cart.getItems().clear();
        cart.setTotalPrice(0.0);

        return Optional.of(cartRepository.save(cart));
    }
}