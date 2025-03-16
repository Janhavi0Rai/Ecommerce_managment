package com.cleartrip.ecommerce_management_project.repository;
import com.cleartrip.ecommerce_management_project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.domain.Sort;
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryIgnoreCase(String category);
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    List<Product> findAll(Sort sort);

    @Query(
            "SELECT p FROM Product p WHERE " +
                    "(:category IS NULL OR p.category = :category) AND " +
                    "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
                    "(:maxPrice IS NULL OR p.price <= :maxPrice)"
    )

    Page<Product> findByFilters(
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );
}
