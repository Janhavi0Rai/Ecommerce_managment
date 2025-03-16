package com.cleartrip.ecommerce_management_project.controller;

import com.cleartrip.ecommerce_management_project.model.Cart;
import com.cleartrip.ecommerce_management_project.service.CartService;
import com.cleartrip.ecommerce_management_project.service.ProductService;
import com.cleartrip.ecommerce_management_project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    // adding to cart
    @PostMapping("/{userId}/add/{productId}")
    public ResponseEntity<Object> addToCart(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        return userService.getUserById(userId)
                .flatMap(user -> productService.getProductById(productId)
                        .flatMap(product -> cartService.addToCart(user, product, quantity)))
                .map(cart -> ResponseEntity.ok().body((Object) cart))
                .orElse(ResponseEntity.badRequest().body(Map.of("message", "Failed to add item to cart")));
    }

    // remove from cart
    @DeleteMapping("/{userId}/remove/{productId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return userService.getUserById(userId)
                .flatMap(user -> productService.getProductById(productId)
                        .flatMap(product -> cartService.removeFromCart(user, product)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // get cart
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .flatMap(cartService::getCartByUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
