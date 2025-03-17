package com.cleartrip.ecommerce_management_project.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carts")
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private Double totalPrice = 0.0;
    
    public void recalculateTotalPrice() {
        this.totalPrice = items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }
    
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
        recalculateTotalPrice();
    }
    
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        recalculateTotalPrice();
    }
}