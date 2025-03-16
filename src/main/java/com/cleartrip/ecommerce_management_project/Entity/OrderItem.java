package com.cleartrip.ecommerce_management_project.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // one order item can have one order
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // one order item can have one product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // quantity of the product
    @Column(nullable = false)
    private Integer quantity;

    // price of the product
    @Column(nullable = false)
    private Double price;
}