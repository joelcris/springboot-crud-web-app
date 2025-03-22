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

import com.webapp.springboot_crud_web_app.model.Order;
import com.webapp.springboot_crud_web_app.model.Order.OrderStatus;
import com.webapp.springboot_crud_web_app.model.OrderItem;
import com.webapp.springboot_crud_web_app.model.Product;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        Order order = new Order();
        order.setCustomerName("John Doe");
        order.setCustomerEmail("john.doe@example.com");
        order.setShippingAddress("123 Test St");
        order.setTotalAmount(BigDecimal.valueOf(99.99));
        order.setStatus(OrderStatus.PENDING);
        
        entityManager.persist(order);
        entityManager.flush();
        
        // Act
        Optional<Order> found = orderRepository.findById(order.getId());
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerName()).isEqualTo("John Doe");
        assertThat(found.get().getCustomerEmail()).isEqualTo("john.doe@example.com");
        assertThat(found.get().getShippingAddress()).isEqualTo("123 Test St");
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void findById_WhenOrderDoesNotExist_ShouldReturnEmptyOptional() {
        // Act
        Optional<Order> found = orderRepository.findById(999L);
        
        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        // Arrange
        Order order1 = new Order();
        order1.setCustomerName("Customer 1");
        order1.setCustomerEmail("customer1@example.com");
        order1.setShippingAddress("Address 1");
        order1.setTotalAmount(BigDecimal.valueOf(10.99));
        order1.setStatus(OrderStatus.PENDING);

        Order order2 = new Order();
        order2.setCustomerName("Customer 2");
        order2.setCustomerEmail("customer2@example.com");
        order2.setShippingAddress("Address 2");
        order2.setTotalAmount(BigDecimal.valueOf(20.99));
        order2.setStatus(OrderStatus.CONFIRMED);
        
        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.flush();
        
        // Act
        List<Order> orders = orderRepository.findAll();
        
        // Assert
        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getCustomerName).containsExactlyInAnyOrder("Customer 1", "Customer 2");
        assertThat(orders).extracting(Order::getStatus).containsExactlyInAnyOrder(OrderStatus.PENDING, OrderStatus.CONFIRMED);
    }

    @Test
    void save_ShouldPersistNewOrder() {
        // Arrange
        Order order = new Order();
        order.setCustomerName("New Customer");
        order.setCustomerEmail("new.customer@example.com");
        order.setShippingAddress("New Address");
        order.setTotalAmount(BigDecimal.valueOf(29.99));
        order.setStatus(OrderStatus.PENDING);
        
        // Act
        Order saved = orderRepository.save(order);
        
        // Assert
        assertThat(saved.getId()).isNotNull();
        
        // Verify persisted in the database
        Order persisted = entityManager.find(Order.class, saved.getId());
        assertThat(persisted).isNotNull();
        assertThat(persisted.getCustomerName()).isEqualTo("New Customer");
        assertThat(persisted.getCustomerEmail()).isEqualTo("new.customer@example.com");
        assertThat(persisted.getShippingAddress()).isEqualTo("New Address");
        assertThat(persisted.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void save_ShouldUpdateExistingOrder() {
        // Arrange
        Order order = new Order();
        order.setCustomerName("Original Name");
        order.setCustomerEmail("original@example.com");
        order.setShippingAddress("Original Address");
        order.setTotalAmount(BigDecimal.valueOf(39.99));
        order.setStatus(OrderStatus.PENDING);
        
        entityManager.persist(order);
        entityManager.flush();
        
        // Update the order
        order.setCustomerName("Updated Name");
        order.setCustomerEmail("updated@example.com");
        order.setStatus(OrderStatus.SHIPPED);
        
        // Act
        Order updated = orderRepository.save(order);
        
        // Assert
        assertThat(updated.getCustomerName()).isEqualTo("Updated Name");
        assertThat(updated.getCustomerEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        
        // Verify updated in the database
        Order fromDb = entityManager.find(Order.class, order.getId());
        assertThat(fromDb.getCustomerName()).isEqualTo("Updated Name");
        assertThat(fromDb.getCustomerEmail()).isEqualTo("updated@example.com");
        assertThat(fromDb.getShippingAddress()).isEqualTo("Original Address"); // Not updated
        assertThat(fromDb.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(39.99)); // Not updated
        assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void delete_ShouldRemoveOrder() {
        // Arrange
        Order order = new Order();
        order.setCustomerName("Customer to Delete");
        order.setCustomerEmail("delete@example.com");
        order.setShippingAddress("Delete Address");
        order.setTotalAmount(BigDecimal.valueOf(59.99));
        order.setStatus(OrderStatus.PENDING);
        
        entityManager.persist(order);
        entityManager.flush();
        
        // Verify exists before deletion
        Order beforeDelete = entityManager.find(Order.class, order.getId());
        assertThat(beforeDelete).isNotNull();
        
        // Act
        orderRepository.delete(order);
        entityManager.flush();
        
        // Assert
        Order afterDelete = entityManager.find(Order.class, order.getId());
        assertThat(afterDelete).isNull();
    }

    @Test
    void deleteById_ShouldRemoveOrder() {
        // Arrange
        Order order = new Order();
        order.setCustomerName("Customer to Delete by ID");
        order.setCustomerEmail("delete.id@example.com");
        order.setShippingAddress("Delete ID Address");
        order.setTotalAmount(BigDecimal.valueOf(69.99));
        order.setStatus(OrderStatus.PENDING);
        
        entityManager.persist(order);
        entityManager.flush();
        
        // Verify exists before deletion
        Order beforeDelete = entityManager.find(Order.class, order.getId());
        assertThat(beforeDelete).isNotNull();
        
        // Act
        orderRepository.deleteById(order.getId());
        entityManager.flush();
        
        // Assert
        Order afterDelete = entityManager.find(Order.class, order.getId());
        assertThat(afterDelete).isNull();
    }

    @Test
    void save_WithOrderItems_ShouldCascadePersist() {
        // Arrange
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setStock(10);
        
        entityManager.persist(product);
        
        Order order = new Order();
        order.setCustomerName("Customer with Items");
        order.setCustomerEmail("customer.items@example.com");
        order.setShippingAddress("Items Address");
        order.setTotalAmount(BigDecimal.valueOf(39.98));
        order.setStatus(OrderStatus.PENDING);
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem.setSubtotal(BigDecimal.valueOf(39.98));
        
        order.getOrderItems().add(orderItem);
        
        // Act
        Order saved = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear(); // Clear to ensure we load from database
        
        // Assert
        Order loadedOrder = entityManager.find(Order.class, saved.getId());
        assertThat(loadedOrder).isNotNull();
        assertThat(loadedOrder.getOrderItems()).hasSize(1);
        
        OrderItem loadedItem = loadedOrder.getOrderItems().get(0);
        assertThat(loadedItem.getId()).isNotNull();
        assertThat(loadedItem.getOrder()).isEqualTo(loadedOrder);
        assertThat(loadedItem.getProduct().getId()).isEqualTo(product.getId());
        assertThat(loadedItem.getQuantity()).isEqualTo(2);
        assertThat(loadedItem.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(loadedItem.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(39.98));
    }

    @Test
    void delete_WithOrderItems_ShouldCascadeDelete() {
        // Arrange
        Product product = new Product();
        product.setName("Test Product for Cascade");
        product.setDescription("Test Description for Cascade");
        product.setPrice(BigDecimal.valueOf(29.99));
        product.setStock(15);
        
        entityManager.persist(product);
        
        Order order = new Order();
        order.setCustomerName("Customer with Items to Delete");
        order.setCustomerEmail("cascade.delete@example.com");
        order.setShippingAddress("Cascade Delete Address");
        order.setTotalAmount(BigDecimal.valueOf(59.98));
        order.setStatus(OrderStatus.PENDING);
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(29.99));
        orderItem.setSubtotal(BigDecimal.valueOf(59.98));
        
        order.getOrderItems().add(orderItem);
        
        entityManager.persist(order);
        entityManager.flush();
        
        Long orderItemId = orderItem.getId();
        
        // Act
        orderRepository.delete(order);
        entityManager.flush();
        
        // Assert
        Order deletedOrder = entityManager.find(Order.class, order.getId());
        assertThat(deletedOrder).isNull();
        
        // Order item should be deleted due to cascade
        OrderItem deletedOrderItem = entityManager.find(OrderItem.class, orderItemId);
        assertThat(deletedOrderItem).isNull();
        
        // Product should NOT be deleted (no cascade)
        Product persistedProduct = entityManager.find(Product.class, product.getId());
        assertThat(persistedProduct).isNotNull();
    }
} 