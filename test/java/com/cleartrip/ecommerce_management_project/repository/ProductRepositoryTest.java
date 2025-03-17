package com.cleartrip.ecommerce_management_project.repository;

import com.cleartrip.ecommerce_management_project.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findById_ShouldReturnProduct() {
        // Given
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(99.99);
        product.setCategory("Electronics");
        product.setDescription("Test Description");
        product = entityManager.persistAndFlush(product);

        // When
        Optional<Product> found = productRepository.findById(product.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        assertThat(found.get().getPrice()).isEqualTo(99.99);
    }

    @Test
    void findByCategory_ShouldReturnProductsInCategory() {
        // Given
        Product product1 = new Product();
        product1.setName("Laptop");
        product1.setPrice(999.99);
        product1.setCategory("Electronics");
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setName("T-shirt");
        product2.setPrice(19.99);
        product2.setCategory("Clothing");
        entityManager.persist(product2);

        Product product3 = new Product();
        product3.setName("Smartphone");
        product3.setPrice(499.99);
        product3.setCategory("Electronics");
        entityManager.persist(product3);

        entityManager.flush();

        // When
        List<Product> foundProducts = productRepository.findByCategory("Electronics");

        // Then
        assertThat(foundProducts).hasSize(2);
        assertThat(foundProducts).extracting(Product::getName).containsExactlyInAnyOrder("Laptop", "Smartphone");
    }

    @Test
    void findByPriceBetween_ShouldReturnProductsInPriceRange() {
        // Given
        Product product1 = new Product();
        product1.setName("Budget Phone");
        product1.setPrice(299.99);
        product1.setCategory("Electronics");
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setName("Premium Phone");
        product2.setPrice(999.99);
        product2.setCategory("Electronics");
        entityManager.persist(product2);

        Product product3 = new Product();
        product3.setName("Mid-range Phone");
        product3.setPrice(499.99);
        product3.setCategory("Electronics");
        entityManager.persist(product3);

        entityManager.flush();

        // When
        List<Product> foundProducts = productRepository.findByPriceBetween(250.0, 600.0);

        // Then
        assertThat(foundProducts).hasSize(2);
        assertThat(foundProducts).extracting(Product::getName).containsExactlyInAnyOrder("Budget Phone", "Mid-range Phone");
    }

    @Test
    void findByCategoryAndPriceBetween_ShouldReturnFilteredProducts() {
        // Given
        Product product1 = new Product();
        product1.setName("Budget Laptop");
        product1.setPrice(599.99);
        product1.setCategory("Electronics");
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setName("Premium Laptop");
        product2.setPrice(1999.99);
        product2.setCategory("Electronics");
        entityManager.persist(product2);

        Product product3 = new Product();
        product3.setName("Budget Phone");
        product3.setPrice(299.99);
        product3.setCategory("Electronics");
        entityManager.persist(product3);

        Product product4 = new Product();
        product4.setName("Budget Headphones");
        product4.setPrice(99.99);
        product4.setCategory("Audio");
        entityManager.persist(product4);

        entityManager.flush();

        // When
        List<Product> foundProducts = productRepository.findByCategoryAndPriceBetween("Electronics", 500.0, 1000.0);

        // Then
        assertThat(foundProducts).hasSize(1);
        assertThat(foundProducts.get(0).getName()).isEqualTo("Budget Laptop");
    }

    @Test
    void findAll_WithSorting_ShouldReturnSortedProducts() {
        // Given
        Product product1 = new Product();
        product1.setName("B-Product");
        product1.setPrice(299.99);
        entityManager.persist(product1);

        Product product2 = new Product();
        product2.setName("A-Product");
        product2.setPrice(199.99);
        entityManager.persist(product2);

        Product product3 = new Product();
        product3.setName("C-Product");
        product3.setPrice(399.99);
        entityManager.persist(product3);

        entityManager.flush();

        // When - Sort by name ascending
        List<Product> ascProducts = productRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        // Then
        assertThat(ascProducts).hasSize(3);
        assertThat(ascProducts.get(0).getName()).isEqualTo("A-Product");
        assertThat(ascProducts.get(1).getName()).isEqualTo("B-Product");
        assertThat(ascProducts.get(2).getName()).isEqualTo("C-Product");

        // When - Sort by price descending
        List<Product> descProducts = productRepository.findAll(Sort.by(Sort.Direction.DESC, "price"));

        // Then
        assertThat(descProducts).hasSize(3);
        assertThat(descProducts.get(0).getName()).isEqualTo("C-Product");
        assertThat(descProducts.get(1).getName()).isEqualTo("B-Product");
        assertThat(descProducts.get(2).getName()).isEqualTo("A-Product");
    }

    @Test
    void findAll_WithPagination_ShouldReturnPagedResults() {
        // Given
        for (int i = 1; i <= 20; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setPrice(i * 10.0);
            product.setCategory(i % 2 == 0 ? "Electronics" : "Clothing");
            entityManager.persist(product);
        }
        entityManager.flush();

        // When - Get first page (10 items)
        Page<Product> firstPage = productRepository.findAll(PageRequest.of(0, 10));

        // Then
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(20);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.isLast()).isFalse();

        // When - Get second page (10 items)
        Page<Product> secondPage = productRepository.findAll(PageRequest.of(1, 10));

        // Then
        assertThat(secondPage.getContent()).hasSize(10);
        assertThat(secondPage.isFirst()).isFalse();
        assertThat(secondPage.isLast()).isTrue();
    }

    @Test
    void save_ShouldPersistProduct() {
        // Given
        Product product = new Product();
        product.setName("New Product");
        product.setPrice(149.99);
        product.setCategory("Home");
        product.setDescription("New Description");

        // When
        Product savedProduct = productRepository.save(product);
        entityManager.flush();

        // Then
        Product found = entityManager.find(Product.class, savedProduct.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("New Product");
        assertThat(found.getPrice()).isEqualTo(149.99);
        assertThat(found.getCategory()).isEqualTo("Home");
    }

    @Test
    void delete_ShouldRemoveProduct() {
        // Given
        Product product = new Product();
        product.setName("Product to Delete");
        product.setPrice(99.99);
        product = entityManager.persistAndFlush(product);
        Long productId = product.getId();

        // When
        productRepository.deleteById(productId);
        entityManager.flush();

        // Then
        Product found = entityManager.find(Product.class, productId);
        assertThat(found).isNull();
    }

    @Test
    void existsById_WhenProductExists_ShouldReturnTrue() {
        // Given
        Product product = new Product();
        product.setName("Test Product");
        product = entityManager.persistAndFlush(product);

        // When
        boolean exists = productRepository.existsById(product.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenProductDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = productRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }
} 