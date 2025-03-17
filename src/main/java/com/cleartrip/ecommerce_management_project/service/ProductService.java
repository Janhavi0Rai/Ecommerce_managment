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

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

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
    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setDescription(updatedProduct.getDescription());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    existingProduct.setCategory(updatedProduct.getCategory());

                    // updating the inventory if provided
                    if (updatedProduct.getInventory() != null) {
                        existingProduct.getInventory().setQuantity(
                                updatedProduct.getInventory().getQuantity()
                        );
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
        return productRepository.findByCategoryIgnoreCase(category);
    }

    // filtering the products by the category and price range
    public Page<Product> filterProducts(
            String category,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByFilters(category, minPrice, maxPrice, pageable);
    }

    // getting all the products with help of pagination
    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    // sorting the products by the price
    public List<Product> sortProducts(String order){
        if(order.equals("asc")){
            return productRepository.findAll(Sort.by(Sort.Direction.ASC, "price"));
        }else{
            return productRepository.findAll(Sort.by(Sort.Direction.DESC, "price"));
        }
    }
}