package com.cleartrip.ecommerce_management_project.controller;

import com.cleartrip.ecommerce_management_project.service.OrderService;
import com.cleartrip.ecommerce_management_project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // place the order
    @PostMapping("/{userId}/place")
    public ResponseEntity<Object> placeOrder(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .flatMap(orderService::placeOrder)
                .map(order -> ResponseEntity.ok().body((Object) order))
                .orElse(ResponseEntity.badRequest().body(Map.of("message", "Failed to place order")));
    }

    // get order by id
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // get orders by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(orderService.getOrdersByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}