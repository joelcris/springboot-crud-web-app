package com.webapp.springboot_crud_web_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webapp.springboot_crud_web_app.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
} 