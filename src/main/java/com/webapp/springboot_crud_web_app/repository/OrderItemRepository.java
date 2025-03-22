package com.webapp.springboot_crud_web_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webapp.springboot_crud_web_app.model.OrderItem;

/**
 * Repository interface for OrderItem entities.
 * Extends JpaRepository to provide basic CRUD operations.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
} 