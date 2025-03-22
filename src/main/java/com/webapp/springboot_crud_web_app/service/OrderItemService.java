package com.webapp.springboot_crud_web_app.service;

import java.util.List;

import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;

/**
 * Service interface for managing OrderItem entities.
 */
public interface OrderItemService {

    /**
     * Retrieves all order items.
     *
     * @return a list of all order items
     */
    List<OrderItemDTO> findAll();

    /**
     * Retrieves an order item by its ID.
     *
     * @param id the ID of the order item to retrieve
     * @return the order item with the given ID
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the order item is not found
     */
    OrderItemDTO findById(Long id);

    /**
     * Creates a new order item.
     *
     * @param orderItemDTO the order item data
     * @param orderId      the ID of the order to which the item belongs
     * @param productId    the ID of the product for the order item
     * @return the created order item
     */
    OrderItemDTO create(OrderItemDTO orderItemDTO, Long orderId, Long productId);

    /**
     * Updates an existing order item.
     *
     * @param orderItemDTO the order item data with updated fields
     * @param id           the ID of the order item to update
     * @param productId    the ID of the product for the order item
     * @return the updated order item
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the order item is not found
     */
    OrderItemDTO update(OrderItemDTO orderItemDTO, Long id, Long productId);

    /**
     * Deletes an order item by its ID.
     *
     * @param id the ID of the order item to delete
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the order item is not found
     */
    void delete(Long id);
} 