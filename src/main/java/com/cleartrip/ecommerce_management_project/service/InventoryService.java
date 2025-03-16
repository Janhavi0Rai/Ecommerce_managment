package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.Inventory;
import com.cleartrip.ecommerce_management_project.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface InventoryService extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);
}
