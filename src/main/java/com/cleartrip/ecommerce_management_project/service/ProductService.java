package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.HttpStatus;
// import com.cleartrip.ecommerce.model.User;
import com.cleartrip.ecommerce_management_project.model.UserRole;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/products")
public class ProductService {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // creating a new product
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product, @RequestParam Long userId) {
        return userService.getUserById(userId)
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .map(user -> ResponseEntity.ok(productService.createProduct(product)))
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    // updating the product
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product, @RequestParam Long userId) {
        return userService.getUserById(userId)
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .map(user -> productService.updateProduct(id, product)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    // deleting the product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, @RequestParam Long userId) {
        return userService.getUserById(userId)
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .map(user -> productService.deleteProduct(id)
                        ? ResponseEntity.ok().build()
                        : ResponseEntity.notFound().build())
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    // getting all the products with help of pagination
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    // getting the product by the id of product
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // searching the product by the category
    @GetMapping("/search/category")
    public ResponseEntity<List<Product>> searchByCategory(@RequestParam String category) {
        return ResponseEntity.ok(productService.searchByCategory(category));
    }

    // filtering the products with help of pagination
    @GetMapping("/filter")
    public ResponseEntity<Page<Product>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.filterProducts(category, minPrice, maxPrice, page, size));
    }

    // sorting the products by the price
    @GetMapping("/sort")
    public ResponseEntity<List<Product>>sortProducts(@RequestParam(required = true) String order){
        return ResponseEntity.ok(productService.sortProducts(order));
    }
}
