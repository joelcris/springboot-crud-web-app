package com.webapp.springboot_crud_web_app.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.webapp.springboot_crud_web_app.model.Product;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setStock(10);
        
        entityManager.persist(product);
        entityManager.flush();
        
        // Act
        Optional<Product> found = productRepository.findById(product.getId());
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        assertThat(found.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        assertThat(found.get().getStock()).isEqualTo(10);
    }

    @Test
    void findById_WhenProductDoesNotExist_ShouldReturnEmptyOptional() {
        // Act
        Optional<Product> found = productRepository.findById(999L);
        
        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        // Arrange
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setDescription("Description 1");
        product1.setPrice(BigDecimal.valueOf(10.99));
        product1.setStock(5);

        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setDescription("Description 2");
        product2.setPrice(BigDecimal.valueOf(20.99));
        product2.setStock(10);
        
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.flush();
        
        // Act
        List<Product> products = productRepository.findAll();
        
        // Assert
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Product 1", "Product 2");
    }

    @Test
    void save_ShouldPersistNewProduct() {
        // Arrange
        Product product = new Product();
        product.setName("New Product");
        product.setDescription("New Description");
        product.setPrice(BigDecimal.valueOf(29.99));
        product.setStock(15);
        
        // Act
        Product saved = productRepository.save(product);
        
        // Assert
        assertThat(saved.getId()).isNotNull();
        
        // Verify persisted in the database
        Product persisted = entityManager.find(Product.class, saved.getId());
        assertThat(persisted).isNotNull();
        assertThat(persisted.getName()).isEqualTo("New Product");
        assertThat(persisted.getDescription()).isEqualTo("New Description");
        assertThat(persisted.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
        assertThat(persisted.getStock()).isEqualTo(15);
    }

    @Test
    void save_ShouldUpdateExistingProduct() {
        // Arrange
        Product product = new Product();
        product.setName("Original Name");
        product.setDescription("Original Description");
        product.setPrice(BigDecimal.valueOf(39.99));
        product.setStock(20);
        
        entityManager.persist(product);
        entityManager.flush();
        
        // Update the product
        product.setName("Updated Name");
        product.setPrice(BigDecimal.valueOf(49.99));
        product.setStock(25);
        
        // Act
        Product updated = productRepository.save(product);
        
        // Assert
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
        assertThat(updated.getStock()).isEqualTo(25);
        
        // Verify updated in the database
        Product fromDb = entityManager.find(Product.class, product.getId());
        assertThat(fromDb.getName()).isEqualTo("Updated Name");
        assertThat(fromDb.getDescription()).isEqualTo("Original Description"); // Not updated
        assertThat(fromDb.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
        assertThat(fromDb.getStock()).isEqualTo(25);
    }

    @Test
    void delete_ShouldRemoveProduct() {
        // Arrange
        Product product = new Product();
        product.setName("Product to Delete");
        product.setDescription("To be deleted");
        product.setPrice(BigDecimal.valueOf(59.99));
        product.setStock(30);
        
        entityManager.persist(product);
        entityManager.flush();
        
        // Verify exists before deletion
        Product beforeDelete = entityManager.find(Product.class, product.getId());
        assertThat(beforeDelete).isNotNull();
        
        // Act
        productRepository.delete(product);
        entityManager.flush();
        
        // Assert
        Product afterDelete = entityManager.find(Product.class, product.getId());
        assertThat(afterDelete).isNull();
    }

    @Test
    void deleteById_ShouldRemoveProduct() {
        // Arrange
        Product product = new Product();
        product.setName("Product to Delete by ID");
        product.setDescription("To be deleted by ID");
        product.setPrice(BigDecimal.valueOf(69.99));
        product.setStock(35);
        
        entityManager.persist(product);
        entityManager.flush();
        
        // Verify exists before deletion
        Product beforeDelete = entityManager.find(Product.class, product.getId());
        assertThat(beforeDelete).isNotNull();
        
        // Act
        productRepository.deleteById(product.getId());
        entityManager.flush();
        
        // Assert
        Product afterDelete = entityManager.find(Product.class, product.getId());
        assertThat(afterDelete).isNull();
    }
} 