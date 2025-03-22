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

import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;
import com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException;
import com.webapp.springboot_crud_web_app.mapper.OrderItemMapper;
import com.webapp.springboot_crud_web_app.model.Order;
import com.webapp.springboot_crud_web_app.model.Order.OrderStatus;
import com.webapp.springboot_crud_web_app.model.OrderItem;
import com.webapp.springboot_crud_web_app.model.Product;
import com.webapp.springboot_crud_web_app.repository.OrderItemRepository;
import com.webapp.springboot_crud_web_app.repository.OrderRepository;
import com.webapp.springboot_crud_web_app.repository.ProductRepository;
import com.webapp.springboot_crud_web_app.service.impl.OrderItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private Order order;
    private Product product;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private OrderItemDTO orderItemDTO1;
    private OrderItemDTO orderItemDTO2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Set up product
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setStock(100);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        // Set up order
        order = new Order();
        order.setId(1L);
        order.setCustomerName("John Doe");
        order.setCustomerEmail("john.doe@example.com");
        order.setShippingAddress("123 Main St, Anytown");
        order.setTotalAmount(BigDecimal.valueOf(39.98));
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // Set up order items
        orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        orderItem1.setOrder(order);
        orderItem1.setProduct(product);
        orderItem1.setQuantity(2);
        orderItem1.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem1.setSubtotal(BigDecimal.valueOf(39.98));

        orderItem2 = new OrderItem();
        orderItem2.setId(2L);
        orderItem2.setOrder(order);
        orderItem2.setProduct(product);
        orderItem2.setQuantity(3);
        orderItem2.setUnitPrice(BigDecimal.valueOf(19.99));
        orderItem2.setSubtotal(BigDecimal.valueOf(59.97));

        // Set up DTOs
        orderItemDTO1 = OrderItemDTO.builder()
                .id(1L)
                .orderId(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(39.98))
                .build();

        orderItemDTO2 = OrderItemDTO.builder()
                .id(2L)
                .orderId(1L)
                .productId(1L)
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(59.97))
                .build();
    }

    @Test
    void findAll_ShouldReturnAllOrderItems() {
        // Arrange
        List<OrderItem> orderItems = Arrays.asList(orderItem1, orderItem2);
        when(orderItemRepository.findAll()).thenReturn(orderItems);
        when(orderItemMapper.toDTO(orderItem1)).thenReturn(orderItemDTO1);
        when(orderItemMapper.toDTO(orderItem2)).thenReturn(orderItemDTO2);

        // Act
        List<OrderItemDTO> result = orderItemService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(orderItemDTO1, result.get(0));
        assertEquals(orderItemDTO2, result.get(1));
        verify(orderItemRepository).findAll();
        verify(orderItemMapper, times(2)).toDTO(any(OrderItem.class));
    }

    @Test
    void findById_WhenOrderItemExists_ShouldReturnOrderItem() {
        // Arrange
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem1));
        when(orderItemMapper.toDTO(orderItem1)).thenReturn(orderItemDTO1);

        // Act
        OrderItemDTO result = orderItemService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(orderItemDTO1, result);
        verify(orderItemRepository).findById(1L);
        verify(orderItemMapper).toDTO(orderItem1);
    }

    @Test
    void findById_WhenOrderItemDoesNotExist_ShouldThrowException() {
        // Arrange
        when(orderItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderItemService.findById(999L));
        verify(orderItemRepository).findById(999L);
        verify(orderItemMapper, never()).toDTO(any(OrderItem.class));
    }

    @Test
    void create_WhenOrderAndProductExist_ShouldCreateOrderItem() {
        // Arrange
        OrderItemDTO inputDTO = OrderItemDTO.builder()
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(39.98))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderItemMapper.toEntity(inputDTO, order, product)).thenReturn(orderItem1);
        when(orderItemRepository.save(orderItem1)).thenReturn(orderItem1);
        when(orderItemMapper.toDTO(orderItem1)).thenReturn(orderItemDTO1);

        // Act
        OrderItemDTO result = orderItemService.create(inputDTO, 1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(orderItemDTO1, result);
        verify(orderRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(orderItemMapper).toEntity(inputDTO, order, product);
        verify(orderItemRepository).save(orderItem1);
        verify(orderItemMapper).toDTO(orderItem1);
    }

    @Test
    void create_WhenOrderDoesNotExist_ShouldThrowException() {
        // Arrange
        OrderItemDTO inputDTO = OrderItemDTO.builder()
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(39.98))
                .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderItemService.create(inputDTO, 999L, 1L));
        verify(orderRepository).findById(999L);
        verify(productRepository, never()).findById(anyLong());
        verify(orderItemMapper, never()).toEntity(any(), any(), any());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void create_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        OrderItemDTO inputDTO = OrderItemDTO.builder()
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(39.98))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderItemService.create(inputDTO, 1L, 999L));
        verify(orderRepository).findById(1L);
        verify(productRepository).findById(999L);
        verify(orderItemMapper, never()).toEntity(any(), any(), any());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void update_WhenOrderItemAndProductExist_ShouldUpdateOrderItem() {
        // Arrange
        OrderItemDTO inputDTO = OrderItemDTO.builder()
                .quantity(4)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(79.96))
                .build();

        OrderItem updatedOrderItem = new OrderItem();
        updatedOrderItem.setId(1L);
        updatedOrderItem.setOrder(order);
        updatedOrderItem.setProduct(product);
        updatedOrderItem.setQuantity(4);
        updatedOrderItem.setUnitPrice(BigDecimal.valueOf(19.99));
        updatedOrderItem.setSubtotal(BigDecimal.valueOf(79.96));

        OrderItemDTO updatedDTO = OrderItemDTO.builder()
                .id(1L)
                .orderId(1L)
                .productId(1L)
                .quantity(4)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(79.96))
                .build();

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(orderItemMapper).updateEntityFromDTO(inputDTO, orderItem1, product);
        when(orderItemRepository.save(orderItem1)).thenReturn(updatedOrderItem);
        when(orderItemMapper.toDTO(updatedOrderItem)).thenReturn(updatedDTO);

        // Act
        OrderItemDTO result = orderItemService.update(inputDTO, 1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(updatedDTO, result);
        verify(orderItemRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(orderItemMapper).updateEntityFromDTO(inputDTO, orderItem1, product);
        verify(orderItemRepository).save(orderItem1);
        verify(orderItemMapper).toDTO(updatedOrderItem);
    }

    @Test
    void update_WhenOrderItemDoesNotExist_ShouldThrowException() {
        // Arrange
        OrderItemDTO inputDTO = OrderItemDTO.builder()
                .quantity(4)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(79.96))
                .build();

        when(orderItemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderItemService.update(inputDTO, 999L, 1L));
        verify(orderItemRepository).findById(999L);
        verify(productRepository, never()).findById(anyLong());
        verify(orderItemMapper, never()).updateEntityFromDTO(any(), any(), any());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void update_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        OrderItemDTO inputDTO = OrderItemDTO.builder()
                .quantity(4)
                .unitPrice(BigDecimal.valueOf(19.99))
                .subtotal(BigDecimal.valueOf(79.96))
                .build();

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem1));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderItemService.update(inputDTO, 1L, 999L));
        verify(orderItemRepository).findById(1L);
        verify(productRepository).findById(999L);
        verify(orderItemMapper, never()).updateEntityFromDTO(any(), any(), any());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void delete_WhenOrderItemExists_ShouldDeleteOrderItem() {
        // Arrange
        when(orderItemRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderItemRepository).deleteById(1L);

        // Act
        orderItemService.delete(1L);

        // Assert
        verify(orderItemRepository).existsById(1L);
        verify(orderItemRepository).deleteById(1L);
    }

    @Test
    void delete_WhenOrderItemDoesNotExist_ShouldThrowException() {
        // Arrange
        when(orderItemRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderItemService.delete(999L));
        verify(orderItemRepository).existsById(999L);
        verify(orderItemRepository, never()).deleteById(anyLong());
    }
} 