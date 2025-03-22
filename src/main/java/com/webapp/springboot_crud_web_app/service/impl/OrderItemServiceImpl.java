package com.webapp.springboot_crud_web_app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;
import com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException;
import com.webapp.springboot_crud_web_app.mapper.OrderItemMapper;
import com.webapp.springboot_crud_web_app.model.Order;
import com.webapp.springboot_crud_web_app.model.OrderItem;
import com.webapp.springboot_crud_web_app.model.Product;
import com.webapp.springboot_crud_web_app.repository.OrderItemRepository;
import com.webapp.springboot_crud_web_app.repository.OrderRepository;
import com.webapp.springboot_crud_web_app.repository.ProductRepository;
import com.webapp.springboot_crud_web_app.service.OrderItemService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the OrderItemService interface.
 */
@Service
@Transactional
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;

    @Autowired
    public OrderItemServiceImpl(OrderItemRepository orderItemRepository,
                                  OrderRepository orderRepository,
                                  ProductRepository productRepository,
                                  OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemDTO> findAll() {
        log.info("Fetching all order items");
        List<OrderItemDTO> orderItems = orderItemRepository.findAll()
                .stream()
                .map(orderItemMapper::toDTO)
                .collect(Collectors.toList());
        log.info("Successfully fetched {} order items", orderItems.size());
        return orderItems;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemDTO findById(Long id) {
        log.info("Fetching order item by ID: {}", id);
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", id));
        OrderItemDTO orderItemDTO = orderItemMapper.toDTO(orderItem);
        log.info("Successfully fetched order item: {}", orderItemDTO);
        return orderItemDTO;
    }

    @Override
    public OrderItemDTO create(OrderItemDTO orderItemDTO, Long orderId, Long productId) {
        log.info("Creating order item for order ID: {} and product ID: {}", orderId, productId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        OrderItem orderItem = orderItemMapper.toEntity(orderItemDTO, order, product);
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        OrderItemDTO resultDTO = orderItemMapper.toDTO(savedOrderItem);

        log.info("Successfully created order item with ID: {}", resultDTO.getId());
        return resultDTO;
    }

    @Override
    public OrderItemDTO update(OrderItemDTO orderItemDTO, Long id, Long productId) {
        log.info("Updating order item with ID: {}", id);

        OrderItem existingOrderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", id));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        orderItemMapper.updateEntityFromDTO(orderItemDTO, existingOrderItem, product);
        OrderItem updatedOrderItem = orderItemRepository.save(existingOrderItem);
        OrderItemDTO resultDTO = orderItemMapper.toDTO(updatedOrderItem);

        log.info("Successfully updated order item with ID: {}", resultDTO.getId());
        return resultDTO;
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting order item with ID: {}", id);
        if (!orderItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("OrderItem", "id", id);
        }
        orderItemRepository.deleteById(id);
        log.info("Successfully deleted order item with ID: {}", id);
    }
} 