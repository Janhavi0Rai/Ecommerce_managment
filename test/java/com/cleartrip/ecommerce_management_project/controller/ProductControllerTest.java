package com.cleartrip.ecommerce_management_project.controller;

import com.cleartrip.ecommerce_management_project.model.Product;
import com.cleartrip.ecommerce_management_project.model.User;
import com.cleartrip.ecommerce_management_project.model.UserRole;
import com.cleartrip.ecommerce_management_project.service.ProductService;
import com.cleartrip.ecommerce_management_project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserService userService;

    private Product testProduct;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        testProduct.setCategory("Electronics");
        testProduct.setDescription("Test Description");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(UserRole.ADMIN);
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(adminUser));
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")));
        
        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(adminUser));
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(Optional.of(testProduct));

        mockMvc.perform(put("/api/products/1")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")));
        
        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void deleteProduct_ShouldReturnOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(adminUser));
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/1")
                .param("userId", "1"))
                .andExpect(status().isOk());
        
        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void getAllProducts_ShouldReturnPageOfProducts() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productService.getAllProducts(anyInt(), anyInt())).thenReturn(productPage);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Test Product")));
        
        verify(productService, times(1)).getAllProducts(anyInt(), anyInt());
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")));
        
        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void searchByCategory_ShouldReturnProductList() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        
        when(productService.searchByCategory("Electronics")).thenReturn(products);

        mockMvc.perform(get("/api/products/search/category")
                .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("Electronics")));
        
        verify(productService, times(1)).searchByCategory("Electronics");
    }

    @Test
    void filterProducts_ShouldReturnFilteredProducts() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productService.filterProducts(anyString(), any(), any(), anyInt(), anyInt())).thenReturn(productPage);

        mockMvc.perform(get("/api/products/filter")
                .param("category", "Electronics")
                .param("minPrice", "50.0")
                .param("maxPrice", "150.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        
        verify(productService, times(1)).filterProducts(eq("Electronics"), eq(50.0), eq(150.0), anyInt(), anyInt());
    }

    @Test
    void sortProducts_ShouldReturnSortedProducts() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        
        when(productService.sortProducts("asc")).thenReturn(products);

        mockMvc.perform(get("/api/products/sort")
                .param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        
        verify(productService, times(1)).sortProducts("asc");
    }

    @Test
    void createProduct_WhenUserNotAdmin_ShouldReturnForbidden() throws Exception {
        User regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(UserRole.CUSTOMER);
        
        when(userService.getUserById(2L)).thenReturn(Optional.of(regularUser));

        mockMvc.perform(post("/api/products")
                .param("userId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isForbidden());
        
        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }
} 