package com.cleartrip.ecommerce_management_project.controller;

import com.cleartrip.ecommerce_management_project.model.User;
import com.cleartrip.ecommerce_management_project.model.Product;
import com.cleartrip.ecommerce_management_project.model.Cart;
import com.cleartrip.ecommerce_management_project.model.CartItem;
import com.cleartrip.ecommerce_management_project.service.CartService;
import com.cleartrip.ecommerce_management_project.service.ProductService;
import com.cleartrip.ecommerce_management_project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam int quantity) {
        
        System.out.println("=== ADD TO CART REQUEST ===");
        System.out.println("userId: " + userId);
        System.out.println("productId: " + productId);
        System.out.println("quantity: " + quantity);
        
        try {
            // Check if user exists
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                System.out.println("ERROR: User not found with ID: " + userId);
                return ResponseEntity.badRequest().body("User not found");
            }
            System.out.println("User found: " + userOpt.get().getUsername());
            
            // Check if product exists
            Optional<Product> productOpt = productService.getProductById(productId);
            if (productOpt.isEmpty()) {
                System.out.println("ERROR: Product not found with ID: " + productId);
                return ResponseEntity.badRequest().body("Product not found");
            }
            System.out.println("Product found: " + productOpt.get().getName());
            
            // Try to add to cart
            System.out.println("Adding to cart...");
            Cart updatedCart = cartService.addItemToCart(userId, productId, quantity);
            System.out.println("Successfully added to cart!");
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            System.out.println("ERROR in addToCart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testCartEndpoint(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer quantity) {
        
        StringBuilder response = new StringBuilder("Test endpoint working! Parameters received: ");
        response.append("userId=").append(userId).append(", ");
        response.append("productId=").append(productId).append(", ");
        response.append("quantity=").append(quantity);
        
        return ResponseEntity.ok(response.toString());
    }
}
