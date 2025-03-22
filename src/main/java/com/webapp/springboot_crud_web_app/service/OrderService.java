package com.webapp.springboot_crud_web_app.service;

import java.util.List;

import com.webapp.springboot_crud_web_app.dto.OrderDTO;

/**
 * Service interface for managing Order entities.
 */
public interface OrderService {

    /**
     * Retrieves all orders.
     *
     * @return a list of all orders
     */
    List<OrderDTO> findAll();

    /**
     * Retrieves an order by its ID.
     *
     * @param id the ID of the order to retrieve
     * @return the order with the given ID
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the order is not found
     */
    OrderDTO findById(Long id);

    /**
     * Creates a new order.
     *
     * @param orderDTO the order data
     * @return the created order
     */
    OrderDTO create(OrderDTO orderDTO);

    /**
     * Updates an existing order.
     *
     * @param orderDTO the order data with updated fields
     * @param id       the ID of the order to update
     * @return the updated order
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the order is not found
     */
    OrderDTO update(OrderDTO orderDTO, Long id);

    /**
     * Deletes an order by its ID.
     *
     * @param id the ID of the order to delete
     * @throws com.webapp.springboot_crud_web_app.exception.ResourceNotFoundException if the order is not found
     */
    void delete(Long id);
} 