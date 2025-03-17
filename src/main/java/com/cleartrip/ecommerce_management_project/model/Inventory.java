package com.cleartrip.ecommerce_management_project.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    private Long id;

    // one inventory can have one product
    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}