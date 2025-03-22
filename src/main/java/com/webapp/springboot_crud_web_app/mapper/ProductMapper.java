package com.webapp.springboot_crud_web_app.mapper;

import org.springframework.stereotype.Component;

import com.webapp.springboot_crud_web_app.dto.ProductDTO;
import com.webapp.springboot_crud_web_app.model.Product;

@Component
public class ProductMapper {
    
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    public Product toEntity(ProductDTO productDTO) {
        if (productDTO == null) {
            return null;
        }
        
        Product product = new Product();
        
        // Don't set ID for new entities
        if (productDTO.getId() != null) {
            product.setId(productDTO.getId());
        }
        
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        
        // Created and updated timestamps are managed by JPA
        
        return product;
    }
    
    public void updateEntityFromDTO(ProductDTO productDTO, Product product) {
        if (productDTO == null || product == null) {
            return;
        }
        
        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }
        
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        
        if (productDTO.getPrice() != null) {
            product.setPrice(productDTO.getPrice());
        }
        
        if (productDTO.getStock() != null) {
            product.setStock(productDTO.getStock());
        }
    }
} 