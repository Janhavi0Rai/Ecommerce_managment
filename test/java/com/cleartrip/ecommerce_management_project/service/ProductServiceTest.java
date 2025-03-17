package com.cleartrip.ecommerce_management_project.service;

import com.cleartrip.ecommerce_management_project.model.Product;
import com.cleartrip.ecommerce_management_project.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        testProduct.setCategory("Electronics");
        testProduct.setDescription("Test Description");

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Another Product");
        testProduct2.setPrice(49.99);
        testProduct2.setCategory("Home");
        testProduct2.setDescription("Another Description");
    }

    @Test
    void createProduct_ShouldReturnSavedProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.createProduct(testProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.getProductById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.getProductById(99L);

        // Then
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    void getAllProducts_ShouldReturnPageOfProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        Page<Product> productPage = new PageImpl<>(products);
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(productPage);

        // When
        Page<Product> result = productService.getAllProducts(0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getId()).isEqualTo(2L);
        verify(productRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldReturnUpdatedProduct() {
        // Given
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Name");
        updatedProduct.setPrice(149.99);
        updatedProduct.setCategory("Updated Category");
        updatedProduct.setDescription("Updated Description");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Product> result = productService.updateProduct(1L, updatedProduct);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L); // ID should remain the same
        assertThat(result.get().getName()).isEqualTo("Updated Name");
        assertThat(result.get().getPrice()).isEqualTo(149.99);
        assertThat(result.get().getCategory()).isEqualTo("Updated Category");
        assertThat(result.get().getDescription()).isEqualTo("Updated Description");
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldReturnEmpty() {
        // Given
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Name");

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.updateProduct(99L, updatedProduct);

        // Then
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldReturnTrue() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // When
        boolean result = productService.deleteProduct(1L);

        // Then
        assertThat(result).isTrue();
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldReturnFalse() {
        // Given
        when(productRepository.existsById(99L)).thenReturn(false);

        // When
        boolean result = productService.deleteProduct(99L);

        // Then
        assertThat(result).isFalse();
        verify(productRepository, times(1)).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void searchByCategory_ShouldReturnProductsInCategory() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategory("Electronics")).thenReturn(products);

        // When
        List<Product> result = productService.searchByCategory("Electronics");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
        verify(productRepository, times(1)).findByCategory("Electronics");
    }

    @Test
    void filterProducts_WithAllParameters_ShouldReturnFilteredProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        when(productRepository.findByCategoryAndPriceBetween(
                eq("Electronics"), eq(50.0), eq(150.0), any(Pageable.class)))
                .thenReturn(productPage);

        // When
        Page<Product> result = productService.filterProducts("Electronics", 50.0, 150.0, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("Electronics");
        verify(productRepository, times(1)).findByCategoryAndPriceBetween(
                eq("Electronics"), eq(50.0), eq(150.0), any(Pageable.class));
    }

    @Test
    void filterProducts_WithCategoryOnly_ShouldReturnFilteredProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        when(productRepository.findByCategory(eq("Electronics"), any(Pageable.class)))
                .thenReturn(productPage);

        // When
        Page<Product> result = productService.filterProducts("Electronics", null, null, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("Electronics");
        verify(productRepository, times(1)).findByCategory(eq("Electronics"), any(Pageable.class));
    }

    @Test
    void filterProducts_WithPriceRangeOnly_ShouldReturnFilteredProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        when(productRepository.findByPriceBetween(eq(50.0), eq(150.0), any(Pageable.class)))
                .thenReturn(productPage);

        // When
        Page<Product> result = productService.filterProducts(null, 50.0, 150.0, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository, times(1)).findByPriceBetween(eq(50.0), eq(150.0), any(Pageable.class));
    }

    @Test
    void filterProducts_WithNoParameters_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        Page<Product> productPage = new PageImpl<>(products);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        // When
        Page<Product> result = productService.filterProducts(null, null, null, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void sortProducts_ByPriceAscending_ShouldReturnSortedProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct2, testProduct); // Sorted by price ascending
        when(productRepository.findAll(any(Sort.class))).thenReturn(products);

        // When
        List<Product> result = productService.sortProducts("asc");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(49.99);
        assertThat(result.get(1).getPrice()).isEqualTo(99.99);
        verify(productRepository, times(1)).findAll(Sort.by(Sort.Direction.ASC, "price"));
    }

    @Test
    void sortProducts_ByPriceDescending_ShouldReturnSortedProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testProduct2); // Sorted by price descending
        when(productRepository.findAll(any(Sort.class))).thenReturn(products);

        // When
        List<Product> result = productService.sortProducts("desc");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(99.99);
        assertThat(result.get(1).getPrice()).isEqualTo(49.99);
        verify(productRepository, times(1)).findAll(Sort.by(Sort.Direction.DESC, "price"));
    }

    @Test
    void sortProducts_WithInvalidOrder_ShouldDefaultToAscending() {
        // Given
        List<Product> products = Arrays.asList(testProduct2, testProduct); // Sorted by price ascending
        when(productRepository.findAll(any(Sort.class))).thenReturn(products);

        // When
        List<Product> result = productService.sortProducts("invalid");

        // Then
        assertThat(result).hasSize(2);
        verify(productRepository, times(1)).findAll(Sort.by(Sort.Direction.ASC, "price"));
    }
} 