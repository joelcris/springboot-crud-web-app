package com.webapp.springboot_crud_web_app.service;

import java.util.List;

import com.webapp.springboot_crud_web_app.dto.ProductDTO;

/**
 * Service interface for managing Product entities.
 */
public interface ProductService {
    
    /**
     * Retrieves all products.
     * 
     * @return a list of all products
     */
    List<ProductDTO> findAll();
    
    /**
     * Retrieves a product by its ID.
     * 
     * @param id the ID of the product to retrieve
     * @return the product with the given ID
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the product is not found
     */
    ProductDTO findById(Long id);
    
    /**
     * Creates a new product.
     * 
     * @param productDTO the product data
     * @return the created product
     */
    ProductDTO create(ProductDTO productDTO);
    
    /**
     * Updates an existing product.
     * 
     * @param productDTO the product data with updated fields
     * @return the updated product
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the product is not found
     */
    ProductDTO update(ProductDTO productDTO);
    
    /**
     * Deletes a product by its ID.
     * 
     * @param id the ID of the product to delete
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the product is not found
     */
    void delete(Long id);
} 