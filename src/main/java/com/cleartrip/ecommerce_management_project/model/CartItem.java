package com.cleartrip.ecommerce_management_project.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity = 1;
}