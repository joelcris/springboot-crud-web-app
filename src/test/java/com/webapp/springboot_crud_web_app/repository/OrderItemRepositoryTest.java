package com.webapp.springboot_crud_web_app.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.webapp.springboot_crud_web_app.model.Order;
import com.webapp.springboot_crud_web_app.model.Order.OrderStatus;
import com.webapp.springboot_crud_web_app.model.OrderItem;
import com.webapp.springboot_crud_web_app.model.Product;

@DataJpaTest
@ActiveProfiles("test")
class OrderItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        // Create test product
        product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setStock(10);
        entityManager.persist(product);

        // Create test order
        order = new Order();
        order.setCustomerName("John Doe");
        order.setCustomerEmail("john.doe@example.com");
        order.setShippingAddress("123 Test St");
        order.setTotalAmount(BigDecimal.valueOf(39.98));
        order.setStatus(OrderStatus.PENDING);
        entityManager.persist(order);

        entityManager.flush();
    }

    @Test
    void findById_WhenOrderItemExists_ShouldReturnOrderItem() {
        // Arrange
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem.setSubtotal(BigDecimal.valueOf(39.98));
        
        entityManager.persist(orderItem);
        entityManager.flush();
        
        // Act
        Optional<OrderItem> found = orderItemRepository.findById(orderItem.getId());
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getOrder().getId()).isEqualTo(order.getId());
        assertThat(found.get().getProduct().getId()).isEqualTo(product.getId());
        assertThat(found.get().getQuantity()).isEqualTo(2);
        assertThat(found.get().getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(found.get().getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(39.98));
    }

    @Test
    void findById_WhenOrderItemDoesNotExist_ShouldReturnEmptyOptional() {
        // Act
        Optional<OrderItem> found = orderItemRepository.findById(999L);
        
        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllOrderItems() {
        // Arrange
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrder(order);
        orderItem1.setProduct(product);
        orderItem1.setQuantity(2);
        orderItem1.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem1.setSubtotal(BigDecimal.valueOf(39.98));
        
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setOrder(order);
        orderItem2.setProduct(product);
        orderItem2.setQuantity(3);
        orderItem2.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem2.setSubtotal(BigDecimal.valueOf(59.97));
        
        entityManager.persist(orderItem1);
        entityManager.persist(orderItem2);
        entityManager.flush();
        
        // Act
        List<OrderItem> orderItems = orderItemRepository.findAll();
        
        // Assert
        assertThat(orderItems).hasSize(2);
        assertThat(orderItems).extracting(OrderItem::getQuantity).containsExactlyInAnyOrder(2, 3);
        assertThat(orderItems).extracting(item -> item.getSubtotal().doubleValue())
                             .containsExactlyInAnyOrder(39.98, 59.97);
    }

    @Test
    void save_ShouldPersistNewOrderItem() {
        // Arrange
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem.setSubtotal(BigDecimal.valueOf(39.98));
        
        // Act
        OrderItem saved = orderItemRepository.save(orderItem);
        
        // Assert
        assertThat(saved.getId()).isNotNull();
        
        // Verify persisted in the database
        OrderItem persisted = entityManager.find(OrderItem.class, saved.getId());
        assertThat(persisted).isNotNull();
        assertThat(persisted.getOrder().getId()).isEqualTo(order.getId());
        assertThat(persisted.getProduct().getId()).isEqualTo(product.getId());
        assertThat(persisted.getQuantity()).isEqualTo(2);
        assertThat(persisted.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(persisted.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(39.98));
    }

    @Test
    void save_ShouldUpdateExistingOrderItem() {
        // Arrange
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem.setSubtotal(BigDecimal.valueOf(39.98));
        
        entityManager.persist(orderItem);
        entityManager.flush();
        
        // Update the order item
        orderItem.setQuantity(3);
        orderItem.setSubtotal(BigDecimal.valueOf(59.97));
        
        // Act
        OrderItem updated = orderItemRepository.save(orderItem);
        
        // Assert
        assertThat(updated.getQuantity()).isEqualTo(3);
        assertThat(updated.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(59.97));
        
        // Verify updated in the database
        OrderItem fromDb = entityManager.find(OrderItem.class, orderItem.getId());
        assertThat(fromDb.getOrder().getId()).isEqualTo(order.getId()); // Not changed
        assertThat(fromDb.getProduct().getId()).isEqualTo(product.getId()); // Not changed
        assertThat(fromDb.getQuantity()).isEqualTo(3); // Updated
        assertThat(fromDb.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(19.99)); // Not changed
        assertThat(fromDb.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(59.97)); // Updated
    }

    @Test
    void delete_ShouldRemoveOrderItem() {
        // Arrange
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem.setSubtotal(BigDecimal.valueOf(39.98));
        
        entityManager.persist(orderItem);
        entityManager.flush();
        
        // Verify exists before deletion
        OrderItem beforeDelete = entityManager.find(OrderItem.class, orderItem.getId());
        assertThat(beforeDelete).isNotNull();
        
        // Act
        orderItemRepository.delete(orderItem);
        entityManager.flush();
        
        // Assert
        OrderItem afterDelete = entityManager.find(OrderItem.class, orderItem.getId());
        assertThat(afterDelete).isNull();
        
        // Order and Product should remain
        Order persistedOrder = entityManager.find(Order.class, order.getId());
        Product persistedProduct = entityManager.find(Product.class, product.getId());
        assertThat(persistedOrder).isNotNull();
        assertThat(persistedProduct).isNotNull();
    }

    @Test
    void deleteById_ShouldRemoveOrderItem() {
        // Arrange
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem.setSubtotal(BigDecimal.valueOf(39.98));
        
        entityManager.persist(orderItem);
        entityManager.flush();
        
        // Verify exists before deletion
        OrderItem beforeDelete = entityManager.find(OrderItem.class, orderItem.getId());
        assertThat(beforeDelete).isNotNull();
        
        // Act
        orderItemRepository.deleteById(orderItem.getId());
        entityManager.flush();
        
        // Assert
        OrderItem afterDelete = entityManager.find(OrderItem.class, orderItem.getId());
        assertThat(afterDelete).isNull();
        
        // Order and Product should remain
        Order persistedOrder = entityManager.find(Order.class, order.getId());
        Product persistedProduct = entityManager.find(Product.class, product.getId());
        assertThat(persistedOrder).isNotNull();
        assertThat(persistedProduct).isNotNull();
    }

    @Test
    void save_WithInvalidOrder_ShouldFail() {
        // Arrange
        Order nonPersistedOrder = new Order();
        nonPersistedOrder.setCustomerName("Non Persisted");
        nonPersistedOrder.setCustomerEmail("non.persisted@example.com");
        nonPersistedOrder.setShippingAddress("Non Persisted Address");
        nonPersistedOrder.setTotalAmount(BigDecimal.valueOf(39.98));
        nonPersistedOrder.setStatus(OrderStatus.PENDING);
        // Note: We're not persisting this order
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(nonPersistedOrder);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem.setSubtotal(BigDecimal.valueOf(39.98));
        
        // Act & Assert
        // This should throw a transient object exception due to non-persisted order
        assertThat(catchThrowable(() -> orderItemRepository.save(orderItem)))
            .isInstanceOf(Exception.class);
    }

    @Test
    void save_WithInvalidProduct_ShouldFail() {
        // Arrange
        Product nonPersistedProduct = new Product();
        nonPersistedProduct.setName("Non Persisted Product");
        nonPersistedProduct.setDescription("Non Persisted Description");
        nonPersistedProduct.setPrice(BigDecimal.valueOf(29.99));
        nonPersistedProduct.setStock(5);
        // Note: We're not persisting this product
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(nonPersistedProduct);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(29.99));
        orderItem.setSubtotal(BigDecimal.valueOf(59.98));
        
        // Act & Assert
        // This should throw a transient object exception due to non-persisted product
        assertThat(catchThrowable(() -> orderItemRepository.save(orderItem)))
            .isInstanceOf(Exception.class);
    }
    
    // Helper method to catch throwables
    private Throwable catchThrowable(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Throwable t) {
            return t;
        }
    }
} 