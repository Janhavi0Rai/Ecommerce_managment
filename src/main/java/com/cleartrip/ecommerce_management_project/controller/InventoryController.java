package com.cleartrip.ecommerce_management_project.controller;

import com.cleartrip.ecommerce_management_project.model.Inventory;
import com.cleartrip.ecommerce_management_project.model.Product;
import com.cleartrip.ecommerce_management_project.service.InventoryService;
import com.cleartrip.ecommerce_management_project.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    // adding stock to the inventory
    @PostMapping("/{productId}")
    public ResponseEntity<?> addStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        return productService.getProductById(productId)
                .map(product -> ResponseEntity.ok(inventoryService.addStock(product, quantity)))
                .orElse(ResponseEntity.notFound().build());
    }

    // update stock in the inventory
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        return productService.getProductById(productId)
                .flatMap(product -> inventoryService.updateStock(product, quantity))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // delete stock from the inventory
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteStock(@PathVariable Long productId) {
        return productService.getProductById(productId)
                .map(product -> {
                    boolean deleted = inventoryService.deleteStock(product);
                    return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // get all inventory
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    // get inventory by product
    @GetMapping("/{productId}")
    public ResponseEntity<?> getInventoryByProduct(@PathVariable Long productId) {
        return productService.getProductById(productId)
                .flatMap(inventoryService::getInventoryByProduct)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
