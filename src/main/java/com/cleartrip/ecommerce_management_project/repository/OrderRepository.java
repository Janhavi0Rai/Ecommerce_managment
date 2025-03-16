package com.cleartrip.ecommerce_management_project.repository;

import com.cleartrip.ecommerce_management_project.model.Order;
import com.cleartrip.ecommerce_management_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
}