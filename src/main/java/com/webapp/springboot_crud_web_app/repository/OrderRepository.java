package com.webapp.springboot_crud_web_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webapp.springboot_crud_web_app.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
} 