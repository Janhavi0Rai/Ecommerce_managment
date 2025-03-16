package com.cleartrip.ecommerce_management_project.model;

import  com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // one cart can have 1 user
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // one cart can have many cart items
    // child uses JsonManagedReference to avoid infinite recursion
    @JsonManagedReference
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;
}