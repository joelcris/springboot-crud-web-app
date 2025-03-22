package com.webapp.springboot_crud_web_app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webapp.springboot_crud_web_app.dto.ProductDTO;
import com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException;
import com.webapp.springboot_crud_web_app.mapper.ProductMapper;
import com.webapp.springboot_crud_web_app.model.Product;
import com.webapp.springboot_crud_web_app.repository.ProductRepository;
import com.webapp.springboot_crud_web_app.service.ProductService;

/**
 * Implementation of the ProductService interface.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        return productMapper.toDTO(product);
    }

    @Override
    public ProductDTO create(ProductDTO productDTO) {
        // Ensure a new product doesn't have an ID
        productDTO.setId(null);
        
        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        
        return productMapper.toDTO(savedProduct);
    }

    @Override
    public ProductDTO update(ProductDTO productDTO) {
        if (productDTO.getId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null for update operation");
        }
        
        // Check if the product exists
        Product existingProduct = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productDTO.getId()));
        
        // Update the product fields
        productMapper.updateEntityFromDTO(productDTO, existingProduct);
        
        // Save and return the updated product
        Product updatedProduct = productRepository.save(existingProduct);
        
        return productMapper.toDTO(updatedProduct);
    }

    @Override
    public void delete(Long id) {
        // Check if the product exists
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        
        productRepository.deleteById(id);
    }
} 