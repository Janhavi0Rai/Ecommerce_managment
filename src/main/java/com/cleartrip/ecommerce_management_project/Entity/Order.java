package com.cleartrip.ecommerce_management_project.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // many order can have one user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // one order can have many order items
    // parent uses JsonBackReference to avoid infinite recursion
    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    // order date
    @Column(nullable = false)
    private LocalDateTime orderDate;

    // total amount
    @Column(nullable = false)
    private Double totalAmount;

    // order status enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
}