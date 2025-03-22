package com.webapp.springboot_crud_web_app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.webapp.springboot_crud_web_app.dto.OrderDTO;
import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;
import com.webapp.springboot_crud_web_app.exception.BusinessRuleViolationException;
import com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException;
import com.webapp.springboot_crud_web_app.mapper.OrderItemMapper;
import com.webapp.springboot_crud_web_app.mapper.OrderMapper;
import com.webapp.springboot_crud_web_app.model.Order;
import com.webapp.springboot_crud_web_app.model.Order.OrderStatus;
import com.webapp.springboot_crud_web_app.model.OrderItem;
import com.webapp.springboot_crud_web_app.model.Product;
import com.webapp.springboot_crud_web_app.repository.OrderRepository;
import com.webapp.springboot_crud_web_app.repository.ProductRepository;
import com.webapp.springboot_crud_web_app.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderDTO orderDTO1;
    private OrderDTO orderDTO2;
    private Order order1;
    private Order order2;
    private OrderItemDTO orderItemDTO1;
    private OrderItem orderItem1;
    private Product product1;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Set up products
        product1 = new Product();
        product1.setId(1L);
        product1.setName("Test Product");
        product1.setDescription("Description");
        product1.setPrice(BigDecimal.valueOf(19.99));
        product1.setStock(100);

        // Set up order items
        orderItemDTO1 = OrderItemDTO.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(39.98))
                .build();

        orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        orderItem1.setProduct(product1);
        orderItem1.setQuantity(2);
        orderItem1.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem1.setSubtotal(BigDecimal.valueOf(39.98));

        // Set up orders
        order1 = new Order();
        order1.setId(1L);
        order1.setCustomerName("John Doe");
        order1.setCustomerEmail("john.doe@example.com");
        order1.setShippingAddress("123 Main St, Anytown");
        order1.setTotalAmount(BigDecimal.valueOf(39.98));
        order1.setStatus(OrderStatus.PENDING);
        order1.setCreatedAt(now);
        order1.setUpdatedAt(now);
        order1.setOrderItems(Arrays.asList(orderItem1));

        order2 = new Order();
        order2.setId(2L);
        order2.setCustomerName("Jane Smith");
        order2.setCustomerEmail("jane.smith@example.com");
        order2.setShippingAddress("456 Oak Ave, Somewhere");
        order2.setTotalAmount(BigDecimal.valueOf(79.96));
        order2.setStatus(OrderStatus.CONFIRMED);
        order2.setCreatedAt(now);
        order2.setUpdatedAt(now);

        // Set up DTOs
        orderDTO1 = OrderDTO.builder()
                .id(1L)
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .shippingAddress("123 Main St, Anytown")
                .totalAmount(BigDecimal.valueOf(39.98))
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(orderItemDTO1))
                .createdAt(now)
                .updatedAt(now)
                .build();

        orderDTO2 = OrderDTO.builder()
                .id(2L)
                .customerName("Jane Smith")
                .customerEmail("jane.smith@example.com")
                .shippingAddress("456 Oak Ave, Somewhere")
                .totalAmount(BigDecimal.valueOf(79.96))
                .status(OrderStatus.CONFIRMED)
                .orderItems(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Set up order item relationship
        orderItem1.setOrder(order1);
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(order1, order2);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.toDTO(order1)).thenReturn(orderDTO1);
        when(orderMapper.toDTO(order2)).thenReturn(orderDTO2);

        // Act
        List<OrderDTO> result = orderService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(orderDTO1, result.get(0));
        assertEquals(orderDTO2, result.get(1));
        verify(orderRepository).findAll();
        verify(orderMapper, times(2)).toDTO(any(Order.class));
    }

    @Test
    void findById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order1));
        when(orderMapper.toDTO(order1)).thenReturn(orderDTO1);

        // Act
        OrderDTO result = orderService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(orderDTO1, result);
        verify(orderRepository).findById(1L);
        verify(orderMapper).toDTO(order1);
    }

    @Test
    void findById_WhenOrderDoesNotExist_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(999L));
        verify(orderRepository).findById(999L);
        verify(orderMapper, never()).toDTO(any(Order.class));
    }

    @Test
    void create_WithValidOrder_ShouldCreateOrder() {
        // Arrange
        when(orderMapper.toEntity(orderDTO1)).thenReturn(order1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(order1)).thenReturn(order1);
        when(orderItemMapper.toEntity(any(OrderItemDTO.class), any(Order.class), any(Product.class))).thenReturn(orderItem1);
        when(orderMapper.toDTO(order1)).thenReturn(orderDTO1);

        // Act
        OrderDTO result = orderService.create(orderDTO1);

        // Assert
        assertNotNull(result);
        assertEquals(orderDTO1, result);
        verify(orderMapper).toEntity(orderDTO1);
        verify(productRepository).findById(1L);
        verify(orderRepository, times(2)).save(order1);
        verify(productRepository).save(product1);
        verify(orderMapper).toDTO(order1);

        // Verify stock was reduced
        assertEquals(98, product1.getStock());
    }

    @Test
    void create_WithEmptyOrderItems_ShouldThrowException() {
        // Arrange
        OrderDTO emptyOrderDTO = OrderDTO.builder()
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .shippingAddress("123 Main St, Anytown")
                .totalAmount(BigDecimal.valueOf(39.98))
                .status(OrderStatus.PENDING)
                .orderItems(Collections.emptyList())
                .build();

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> orderService.create(emptyOrderDTO));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void create_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        OrderItemDTO largeQuantityItemDTO = OrderItemDTO.builder()
                .productId(1L)
                .quantity(150) // More than available stock (100)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(2998.50))
                .build();

        OrderDTO largeOrderDTO = OrderDTO.builder()
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .shippingAddress("123 Main St, Anytown")
                .totalAmount(BigDecimal.valueOf(2998.50))
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(largeQuantityItemDTO))
                .build();

        Order largeOrder = new Order();
        largeOrder.setCustomerName("John Doe");
        largeOrder.setCustomerEmail("john.doe@example.com");
        
        when(orderMapper.toEntity(largeOrderDTO)).thenReturn(largeOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> orderService.create(largeOrderDTO));
        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_WithInvalidProductId_ShouldThrowException() {
        // Arrange
        OrderItemDTO invalidProductItemDTO = OrderItemDTO.builder()
                .productId(999L) // Non-existent product ID
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(39.98))
                .build();

        OrderDTO invalidOrderDTO = OrderDTO.builder()
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .shippingAddress("123 Main St, Anytown")
                .totalAmount(BigDecimal.valueOf(39.98))
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(invalidProductItemDTO))
                .build();

        Order invalidOrder = new Order();
        invalidOrder.setCustomerName("John Doe");
        invalidOrder.setCustomerEmail("john.doe@example.com");
        
        when(orderMapper.toEntity(invalidOrderDTO)).thenReturn(invalidOrder);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.create(invalidOrderDTO));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void update_WhenOrderExists_ShouldUpdateAndReturnOrder() {
        // Arrange
        OrderDTO updateDTO = OrderDTO.builder()
                .id(1L)
                .customerName("John Doe Updated")
                .customerEmail("john.updated@example.com")
                .shippingAddress("123 Main St Updated")
                .totalAmount(BigDecimal.valueOf(39.98))
                .status(OrderStatus.CONFIRMED)
                .build();

        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setCustomerName("John Doe Updated");
        updatedOrder.setCustomerEmail("john.updated@example.com");
        updatedOrder.setShippingAddress("123 Main St Updated");
        updatedOrder.setTotalAmount(BigDecimal.valueOf(39.98));
        updatedOrder.setStatus(OrderStatus.CONFIRMED);
        updatedOrder.setCreatedAt(now);
        updatedOrder.setUpdatedAt(now);

        OrderDTO updatedDTO = OrderDTO.builder()
                .id(1L)
                .customerName("John Doe Updated")
                .customerEmail("john.updated@example.com")
                .shippingAddress("123 Main St Updated")
                .totalAmount(BigDecimal.valueOf(39.98))
                .status(OrderStatus.CONFIRMED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order1));
        doNothing().when(orderMapper).updateEntityFromDTO(updateDTO, order1);
        when(orderRepository.save(order1)).thenReturn(updatedOrder);
        when(orderMapper.toDTO(updatedOrder)).thenReturn(updatedDTO);

        // Act
        OrderDTO result = orderService.update(updateDTO, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(updatedDTO, result);
        verify(orderRepository).findById(1L);
        verify(orderMapper).updateEntityFromDTO(updateDTO, order1);
        verify(orderRepository).save(order1);
        verify(orderMapper).toDTO(updatedOrder);
    }

    @Test
    void update_WhenOrderDoesNotExist_ShouldThrowException() {
        // Arrange
        OrderDTO updateDTO = OrderDTO.builder()
                .id(999L)
                .customerName("Updated Name")
                .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.update(updateDTO, 999L));
        verify(orderRepository).findById(999L);
        verify(orderMapper, never()).updateEntityFromDTO(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void delete_WhenOrderExists_ShouldDeleteOrder() {
        // Arrange
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);

        // Act
        orderService.delete(1L);

        // Assert
        verify(orderRepository).existsById(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void delete_WhenOrderDoesNotExist_ShouldThrowException() {
        // Arrange
        when(orderRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.delete(999L));
        verify(orderRepository).existsById(999L);
        verify(orderRepository, never()).deleteById(anyLong());
    }
} 