package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.Product;
import com.cleartrip.ecommerce_management_project.model.Inventory;
import com.cleartrip.ecommerce_management_project.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import java.util.ArrayList;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // creating the product
    @Transactional
    public Product createProduct(Product product) {
        if (product.getInventory() != null) {
            product.getInventory().setProduct(product);
        } else {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(0);
            product.setInventory(inventory);
        }
        return productRepository.save(product);
    }

    // updating the product
    @Transactional
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    if (productDetails.getName() != null) {
                        existingProduct.setName(productDetails.getName());
                    }
                    if (productDetails.getPrice() != null) {
                        existingProduct.setPrice(productDetails.getPrice());
                    }
                    if (productDetails.getCategory() != null) {
                        existingProduct.setCategory(productDetails.getCategory());
                    }
                    if (productDetails.getDescription() != null) {
                        existingProduct.setDescription(productDetails.getDescription());
                    }
                    return productRepository.save(existingProduct);
                });
    }

    // deleting the product
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // getting all the products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // getting the product by the id of product
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // searching the product by the name
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // searching the product by the category
    public List<Product> searchByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // filtering the products by the category and price range
    public Page<Product> filterProducts(String category, Double minPrice, Double maxPrice, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        
        if (category != null && minPrice != null && maxPrice != null) {
            return productRepository.findByCategoryAndPriceBetween(category, minPrice, maxPrice, pageRequest);
        } else if (category != null) {
            return productRepository.findByCategory(category, pageRequest);
        } else if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceBetween(minPrice, maxPrice, pageRequest);
        } else {
            return productRepository.findAll(pageRequest);
        }
    }

    // getting all the products with help of pagination
    public Page<Product> getAllProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    // sorting the products by the price
    public List<Product> sortProducts(String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return productRepository.findAll(Sort.by(direction, "price"));
    }
}