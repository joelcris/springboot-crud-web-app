package com.webapp.springboot_crud_web_app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webapp.springboot_crud_web_app.dto.ProductDTO;
import com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException;
import com.webapp.springboot_crud_web_app.mapper.ProductMapper;
import com.webapp.springboot_crud_web_app.model.Product;
import com.webapp.springboot_crud_web_app.repository.ProductRepository;
import com.webapp.springboot_crud_web_app.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product1;
    private Product product2;
    private ProductDTO productDTO1;
    private ProductDTO productDTO2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Set up current time for consistent testing
        now = LocalDateTime.now();

        // Set up test products
        product1 = new Product();
        product1.setId(1L);
        product1.setName("Test Product 1");
        product1.setDescription("Description 1");
        product1.setPrice(BigDecimal.valueOf(19.99));
        product1.setStock(100);
        product1.setCreatedAt(now);
        product1.setUpdatedAt(now);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Test Product 2");
        product2.setDescription("Description 2");
        product2.setPrice(BigDecimal.valueOf(29.99));
        product2.setStock(50);
        product2.setCreatedAt(now);
        product2.setUpdatedAt(now);

        // Set up test DTOs
        productDTO1 = ProductDTO.builder()
                .id(1L)
                .name("Test Product 1")
                .description("Description 1")
                .price(BigDecimal.valueOf(19.99))
                .stock(100)
                .createdAt(now)
                .updatedAt(now)
                .build();

        productDTO2 = ProductDTO.builder()
                .id(2L)
                .name("Test Product 2")
                .description("Description 2")
                .price(BigDecimal.valueOf(29.99))
                .stock(50)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toDTO(product1)).thenReturn(productDTO1);
        when(productMapper.toDTO(product2)).thenReturn(productDTO2);

        // Act
        List<ProductDTO> result = productService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(productDTO1, result.get(0));
        assertEquals(productDTO2, result.get(1));
        verify(productRepository).findAll();
        verify(productMapper, times(2)).toDTO(any(Product.class));
    }

    @Test
    void findById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productMapper.toDTO(product1)).thenReturn(productDTO1);

        // Act
        ProductDTO result = productService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(productDTO1, result);
        verify(productRepository).findById(1L);
        verify(productMapper).toDTO(product1);
    }

    @Test
    void findById_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.findById(999L));
        verify(productRepository).findById(999L);
        verify(productMapper, never()).toDTO(any(Product.class));
    }

    @Test
    void create_ShouldSaveAndReturnNewProduct() {
        // Arrange
        ProductDTO inputDTO = ProductDTO.builder()
                .name("New Product")
                .description("New Description")
                .price(BigDecimal.valueOf(39.99))
                .stock(75)
                .build();

        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setDescription("New Description");
        newProduct.setPrice(BigDecimal.valueOf(39.99));
        newProduct.setStock(75);

        Product savedProduct = new Product();
        savedProduct.setId(3L);
        savedProduct.setName("New Product");
        savedProduct.setDescription("New Description");
        savedProduct.setPrice(BigDecimal.valueOf(39.99));
        savedProduct.setStock(75);
        savedProduct.setCreatedAt(now);
        savedProduct.setUpdatedAt(now);

        ProductDTO savedDTO = ProductDTO.builder()
                .id(3L)
                .name("New Product")
                .description("New Description")
                .price(BigDecimal.valueOf(39.99))
                .stock(75)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(productMapper.toEntity(inputDTO)).thenReturn(newProduct);
        when(productRepository.save(newProduct)).thenReturn(savedProduct);
        when(productMapper.toDTO(savedProduct)).thenReturn(savedDTO);

        // Act
        ProductDTO result = productService.create(inputDTO);

        // Assert
        assertNotNull(result);
        assertEquals(savedDTO, result);
        verify(productMapper).toEntity(inputDTO);
        verify(productRepository).save(newProduct);
        verify(productMapper).toDTO(savedProduct);
    }

    @Test
    void update_WhenProductExists_ShouldUpdateAndReturnProduct() {
        // Arrange
        ProductDTO updateDTO = ProductDTO.builder()
                .id(1L)
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(24.99))
                .stock(120)
                .build();

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(BigDecimal.valueOf(24.99));
        updatedProduct.setStock(120);
        updatedProduct.setCreatedAt(now);
        updatedProduct.setUpdatedAt(now);

        ProductDTO updatedDTO = ProductDTO.builder()
                .id(1L)
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(24.99))
                .stock(120)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        doNothing().when(productMapper).updateEntityFromDTO(updateDTO, product1);
        when(productRepository.save(product1)).thenReturn(updatedProduct);
        when(productMapper.toDTO(updatedProduct)).thenReturn(updatedDTO);

        // Act
        ProductDTO result = productService.update(updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updatedDTO, result);
        verify(productRepository).findById(1L);
        verify(productMapper).updateEntityFromDTO(updateDTO, product1);
        verify(productRepository).save(product1);
        verify(productMapper).toDTO(updatedProduct);
    }

    @Test
    void update_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        ProductDTO updateDTO = ProductDTO.builder()
                .id(999L)
                .name("Updated Product")
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.update(updateDTO));
        verify(productRepository).findById(999L);
        verify(productMapper, never()).updateEntityFromDTO(any(), any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_WhenIdIsNull_ShouldThrowException() {
        // Arrange
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> productService.update(updateDTO));
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    void delete_WhenProductExists_ShouldDeleteProduct() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.delete(1L);

        // Assert
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.delete(999L));
        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }
} 