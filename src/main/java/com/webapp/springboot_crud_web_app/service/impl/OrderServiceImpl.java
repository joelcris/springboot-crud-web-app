package com.webapp.springboot_crud_web_app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webapp.springboot_crud_web_app.dto.OrderDTO;
import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;
import com.webapp.springboot_crud_web_app.exception.BusinessRuleViolationException;
import com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException;
import com.webapp.springboot_crud_web_app.mapper.OrderItemMapper;
import com.webapp.springboot_crud_web_app.mapper.OrderMapper;
import com.webapp.springboot_crud_web_app.model.Order;
import com.webapp.springboot_crud_web_app.model.OrderItem;
import com.webapp.springboot_crud_web_app.model.Product;
import com.webapp.springboot_crud_web_app.repository.OrderRepository;
import com.webapp.springboot_crud_web_app.repository.ProductRepository;
import com.webapp.springboot_crud_web_app.service.OrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the OrderService interface.
 */
@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper, ProductRepository productRepository, OrderItemMapper orderItemMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.productRepository = productRepository;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> findAll() {
        log.info("Fetching all orders");
        List<OrderDTO> orders = orderRepository.findAll()
                .stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
        log.info("Successfully fetched {} orders", orders.size());
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO findById(Long id) {
        log.info("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        OrderDTO orderDTO = orderMapper.toDTO(order);
        log.info("Successfully fetched order: {}", orderDTO);
        return orderDTO;
    }

    @Override
    public OrderDTO create(OrderDTO orderDTO) {
        log.info("Creating new order");
        Order order = orderMapper.toEntity(orderDTO);
        
        // Validate order items and check stock
        List<OrderItemDTO> orderItemDTOs = orderDTO.getOrderItems();
        if (orderItemDTOs == null || orderItemDTOs.isEmpty()) {
            throw new BusinessRuleViolationException("Order must contain at least one item");
        }
        
        // Validate stock for each product before saving the order
        List<Product> productsToUpdate = orderItemDTOs.stream()
                .map(itemDTO -> {
                    Product product = productRepository.findById(itemDTO.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDTO.getProductId()));
                    
                    // Check if there's enough stock
                    if (product.getStock() < itemDTO.getQuantity()) {
                        throw new BusinessRuleViolationException(
                                "Insufficient stock for product '" + product.getName() + 
                                "'. Available: " + product.getStock() + ", Requested: " + itemDTO.getQuantity());
                    }
                    
                    // Update product stock
                    product.setStock(product.getStock() - itemDTO.getQuantity());
                    return product;
                })
                .collect(Collectors.toList());
        
        // Save the order
        Order savedOrder = orderRepository.save(order);
        
        // Create and set order items
        List<OrderItem> orderItems = orderItemDTOs.stream()
                .map(itemDTO -> {
                    Product product = productsToUpdate.stream()
                            .filter(p -> p.getId().equals(itemDTO.getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDTO.getProductId()));
                    
                    // Save product with updated stock
                    productRepository.save(product);
                    
                    OrderItem orderItem = orderItemMapper.toEntity(itemDTO, savedOrder, product);
                    return orderItem;
                })
                .collect(Collectors.toList());
        
        savedOrder.setOrderItems(orderItems);
        orderRepository.save(savedOrder);

        OrderDTO resultDTO = orderMapper.toDTO(savedOrder);
        log.info("Successfully created order with ID: {}", resultDTO.getId());
        return resultDTO;
    }

    @Override
    public OrderDTO update(OrderDTO orderDTO, Long id) {
        log.info("Updating order with ID: {}", id);
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        orderMapper.updateEntityFromDTO(orderDTO, existingOrder);
        Order updatedOrder = orderRepository.save(existingOrder);
        OrderDTO resultDTO = orderMapper.toDTO(updatedOrder);
        log.info("Successfully updated order with ID: {}", resultDTO.getId());
        return resultDTO;
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting order with ID: {}", id);
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", "id", id);
        }
        orderRepository.deleteById(id);
        log.info("Successfully deleted order with ID: {}", id);
    }
} 