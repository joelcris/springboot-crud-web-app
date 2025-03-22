package com.webapp.springboot_crud_web_app.mapper;

import org.springframework.stereotype.Component;

import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;
import com.webapp.springboot_crud_web_app.model.Order;
import com.webapp.springboot_crud_web_app.model.OrderItem;
import com.webapp.springboot_crud_web_app.model.Product;

@Component
public class OrderItemMapper {
    
    public OrderItemDTO toDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .orderId(orderItem.getOrder().getId())
                .productId(orderItem.getProduct().getId())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .subtotal(orderItem.getSubtotal())
                .build();
    }
    
    public OrderItem toEntity(OrderItemDTO orderItemDTO, Order order, Product product) {
        if (orderItemDTO == null) {
            return null;
        }
        
        OrderItem orderItem = new OrderItem();
        
        // Don't set ID for new entities
        if (orderItemDTO.getId() != null) {
            orderItem.setId(orderItemDTO.getId());
        }
        
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(orderItemDTO.getQuantity());
        orderItem.setUnitPrice(orderItemDTO.getUnitPrice());
        orderItem.setSubtotal(orderItemDTO.getSubtotal());
        
        return orderItem;
    }
    
    public void updateEntityFromDTO(OrderItemDTO orderItemDTO, OrderItem orderItem, Product product) {
        if (orderItemDTO == null || orderItem == null) {
            return;
        }
        
        if (product != null) {
            orderItem.setProduct(product);
        }
        
        if (orderItemDTO.getQuantity() != null) {
            orderItem.setQuantity(orderItemDTO.getQuantity());
        }
        
        if (orderItemDTO.getUnitPrice() != null) {
            orderItem.setUnitPrice(orderItemDTO.getUnitPrice());
        }
        
        if (orderItemDTO.getSubtotal() != null) {
            orderItem.setSubtotal(orderItemDTO.getSubtotal());
        }
    }
} 