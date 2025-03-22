package com.webapp.springboot_crud_web_app.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webapp.springboot_crud_web_app.dto.OrderDTO;
import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;
import com.webapp.springboot_crud_web_app.model.Order;
import com.webapp.springboot_crud_web_app.model.OrderItem;

@Component
public class OrderMapper {
    
    private final OrderItemMapper orderItemMapper;
    
    @Autowired
    public OrderMapper(OrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }
    
    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
        
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(orderItemMapper::toDTO)
                .collect(Collectors.toList());
        
        return OrderDTO.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderItems(orderItemDTOs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    public Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return null;
        }
        
        Order order = new Order();
        
        // Don't set ID for new entities
        if (orderDTO.getId() != null) {
            order.setId(orderDTO.getId());
        }
        
        order.setCustomerName(orderDTO.getCustomerName());
        order.setCustomerEmail(orderDTO.getCustomerEmail());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setStatus(orderDTO.getStatus());
        
        // Order items will be set separately
        
        return order;
    }
    
    public void updateEntityFromDTO(OrderDTO orderDTO, Order order) {
        if (orderDTO == null || order == null) {
            return;
        }
        
        if (orderDTO.getCustomerName() != null) {
            order.setCustomerName(orderDTO.getCustomerName());
        }
        
        if (orderDTO.getCustomerEmail() != null) {
            order.setCustomerEmail(orderDTO.getCustomerEmail());
        }
        
        if (orderDTO.getShippingAddress() != null) {
            order.setShippingAddress(orderDTO.getShippingAddress());
        }
        
        if (orderDTO.getTotalAmount() != null) {
            order.setTotalAmount(orderDTO.getTotalAmount());
        }
        
        if (orderDTO.getStatus() != null) {
            order.setStatus(orderDTO.getStatus());
        }
        
        // Order items will be updated separately
    }
    
    // Helper method to merge order items
    public void updateOrderItems(Order order, List<OrderItem> newOrderItems) {
        // Clear existing items and add all new ones
        order.getOrderItems().clear();
        order.getOrderItems().addAll(newOrderItems);
        
        // Set order reference in all items
        for (OrderItem item : newOrderItems) {
            item.setOrder(order);
        }
    }
} 