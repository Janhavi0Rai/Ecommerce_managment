package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.*;
import com.cleartrip.ecommerce_management_project.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private InventoryService inventoryService;


    public Optional<Cart> getOrCreateCart(User user) {
        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isPresent()) {
            return existingCart;
        } else {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setItems(new ArrayList<>());
            return Optional.of(cartRepository.save(newCart));
        }
    }

    // add to cart
    public Optional<Cart> addToCart(User user, Product product, Integer quantity) {
        Optional<Cart> cartOptional = getOrCreateCart(user);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();

            // Check inventory
            Optional<Inventory> inventoryOptional = inventoryService.getInventoryByProduct(product);
            if (inventoryOptional.isEmpty() || inventoryOptional.get().getQuantity() < quantity) {
                return Optional.empty();
            }

            // Check if product already exists in cart
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

    // remove from cart
    public Optional<Cart> removeFromCart(User user, Product product) {
        Optional<Cart> cartOptional = cartRepository.findByUser(user);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            cart.getItems().removeIf(item -> item.getProduct().getId().equals(product.getId()));
            return Optional.of(cartRepository.save(cart));
        }
        return Optional.empty();
    }

    // get cart by user
    public Optional<Cart> getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    // clear cart
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}