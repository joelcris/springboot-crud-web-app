package com.webapp.springboot_crud_web_app.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webapp.springboot_crud_web_app.dto.ProductDTO;
import com.webapp.springboot_crud_web_app.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductService productService;

    @Test
    void createProduct_ValidInput_ReturnsCreatedProduct() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name("Test Product")
                .description("This is a test product")
                .price(BigDecimal.valueOf(19.99))
                .stock(100)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Product"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("This is a test product"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(19.99))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stock").value(100));
    }
    
    @Test
    void getAllProducts_ReturnsProductsList() throws Exception {
        // Create a product first to ensure there's at least one product in the system
        createTestProduct();
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").exists());
    }
    
    @Test
    void getProductById_ExistingProduct_ReturnsProduct() throws Exception {
        // Create a product and get its ID
        ProductDTO createdProduct = createTestProduct();
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/{id}", createdProduct.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdProduct.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(createdProduct.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(createdProduct.getDescription()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(createdProduct.getPrice().doubleValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stock").value(createdProduct.getStock()));
    }
    
    @Test
    void getProductById_NonExistingProduct_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void updateProduct_ValidInput_ReturnsUpdatedProduct() throws Exception {
        // Create a product first
        ProductDTO createdProduct = createTestProduct();
        
        // Prepare update data
        createdProduct.setName("Updated Product Name");
        createdProduct.setPrice(BigDecimal.valueOf(29.99));
        
        mockMvc.perform(MockMvcRequestBuilders.put("/api/products/{id}", createdProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdProduct)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdProduct.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Product Name"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(29.99));
    }
    
    @Test
    void updateProduct_NonExistingProduct_ReturnsNotFound() throws Exception {
        ProductDTO productDTO = createTestProductDTO();
        
        mockMvc.perform(MockMvcRequestBuilders.put("/api/products/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void deleteProduct_ExistingProduct_ReturnsNoContent() throws Exception {
        // Create a product first
        ProductDTO createdProduct = createTestProduct();
        
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/products/{id}", createdProduct.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        // Verify the product is deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/{id}", createdProduct.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void deleteProduct_NonExistingProduct_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/products/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void createProduct_InvalidName_ReturnsBadRequest() throws Exception {
        ProductDTO productDTO = createTestProductDTO();
        productDTO.setName(""); // Empty name - should be rejected
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'name')]").exists());
    }
    
    @Test
    void createProduct_NegativePrice_ReturnsBadRequest() throws Exception {
        ProductDTO productDTO = createTestProductDTO();
        productDTO.setPrice(BigDecimal.valueOf(-10.00)); // Negative price - should be rejected
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'price')]").exists());
    }
    
    @Test
    void createProduct_NegativeStock_ReturnsBadRequest() throws Exception {
        ProductDTO productDTO = createTestProductDTO();
        productDTO.setStock(-5); // Negative stock - should be rejected
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'stock')]").exists());
    }
    
    @Test
    void createProduct_DescriptionTooLong_ReturnsBadRequest() throws Exception {
        ProductDTO productDTO = createTestProductDTO();
        
        // Create a description longer than 1000 characters
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            sb.append("This is a very long description that will exceed the maximum allowed length. ");
        }
        productDTO.setDescription(sb.toString());
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'description')]").exists());
    }
    
    /**
     * Helper method to create a test product DTO without saving it
     */
    private ProductDTO createTestProductDTO() {
        return ProductDTO.builder()
                .name("Test Product")
                .description("This is a test product")
                .price(BigDecimal.valueOf(19.99))
                .stock(100)
                .build();
    }
    
    /**
     * Helper method to create and save a test product
     */
    private ProductDTO createTestProduct() {
        ProductDTO productDTO = createTestProductDTO();
        return productService.create(productDTO);
    }
} 