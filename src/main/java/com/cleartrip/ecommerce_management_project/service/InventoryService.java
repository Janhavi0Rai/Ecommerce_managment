package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.Inventory;
import com.cleartrip.ecommerce_management_project.model.Product;
import com.cleartrip.ecommerce_management_project.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;


    public Inventory addStock(Product product, Integer quantity) {
        Optional<Inventory> existingInventory = inventoryRepository.findByProduct(product);
        if (existingInventory.isPresent()) {
            Inventory inventory = existingInventory.get();
            inventory.setQuantity(inventory.getQuantity() + quantity);
            return inventoryRepository.save(inventory);
        } else {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(quantity);
            return inventoryRepository.save(inventory);
        }
    }

    // update stock
    public Optional<Inventory> updateStock(Product product, Integer quantity) {
        Optional<Inventory> existingInventory = inventoryRepository.findByProduct(product);
        if (existingInventory.isPresent()) {
            Inventory inventory = existingInventory.get();
            inventory.setQuantity(quantity);
            return Optional.of(inventoryRepository.save(inventory));
        }
        return Optional.empty();
    }

    // delete stock
    public boolean deleteStock(Product product) {
        Optional<Inventory> inventory = inventoryRepository.findByProduct(product);
        if (inventory.isPresent()) {
            inventoryRepository.delete(inventory.get());
            return true;
        }
        return false;
    }

    // get all inventory
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // get inventory by product
    public Optional<Inventory> getInventoryByProduct(Product product) {
        return inventoryRepository.findByProduct(product);
    }
}