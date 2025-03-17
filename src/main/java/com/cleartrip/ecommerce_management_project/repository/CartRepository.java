package com.cleartrip.ecommerce_management_project.repository;

import com.cleartrip.ecommerce_management_project.model.Cart;
import com.cleartrip.ecommerce_management_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
    
    Optional<Cart> findByUser(User user);
    
    void deleteByUserId(Long userId);
}
