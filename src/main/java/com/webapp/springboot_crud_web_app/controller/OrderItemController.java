package com.webapp.springboot_crud_web_app.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;
import com.webapp.springboot_crud_web_app.exception.ErrorResponse;
import com.webapp.springboot_crud_web_app.service.OrderItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing OrderItems.
 */
@RestController
@RequestMapping("/api/order-items")
@Tag(name = "OrderItem", description = "OrderItem management APIs")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Autowired
    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    /**
     * GET /api/order-items : Get all order items.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of order items in body
     */
    @GetMapping
    @Operation(summary = "Get all order items", description = "Returns a list of all available order items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of order items",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderItemDTO.class)))
    })
    public ResponseEntity<List<OrderItemDTO>> getAllOrderItems() {
        List<OrderItemDTO> orderItems = orderItemService.findAll();
        return ResponseEntity.ok().body(orderItems);
    }

    /**
     * GET /api/order-items/{id} : Get order item by id.
     *
     * @param id the id of the order item to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the order item, or with status 404 (Not Found)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order item by ID", description = "Returns an order item based on the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved order item"),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    public ResponseEntity<OrderItemDTO> getOrderItemById(
            @Parameter(description = "ID of the order item to retrieve", required = true)
            @PathVariable Long id) {
        OrderItemDTO orderItem = orderItemService.findById(id);
        return ResponseEntity.ok().body(orderItem);
    }

    /**
     * POST /api/order-items : Create a new order item.
     *
     * @param orderItemDTO the order item to create, including orderId and productId
     * @return the ResponseEntity with status 201 (Created) and with body the new order item
     */
    @PostMapping
    @Operation(summary = "Create a new order item", description = "Creates a new order item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order item successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<OrderItemDTO> createOrderItem(
            @Parameter(description = "Order item data to create, including orderId and productId", required = true)
            @Valid @RequestBody OrderItemDTO orderItemDTO) {
        OrderItemDTO result = orderItemService.create(
                orderItemDTO, orderItemDTO.getOrderId(), orderItemDTO.getProductId()); // Extract orderId and productId from DTO
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.getId())
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    /**
     * PUT /api/order-items/{id} : Update an existing order item.
     *
     * @param id           the id of the order item to update
     * @param orderItemDTO the order item to update, including productId if needed
     * @return the ResponseEntity with status 200 (OK) and with body the updated order item
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing order item", description = "Updates the order item with the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order item successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    public ResponseEntity<OrderItemDTO> updateOrderItem(
            @Parameter(description = "ID of the order item to update", required = true) @PathVariable Long id,
            @Parameter(description = "Updated order item data, including productId if needed", required = true) @Valid @RequestBody OrderItemDTO orderItemDTO) {
        orderItemDTO.setId(id);
        OrderItemDTO result = orderItemService.update(
                orderItemDTO, id, orderItemDTO.getProductId()); // Extract productId from DTO
        return ResponseEntity.ok().body(result);
    }

    /**
     * DELETE /api/order-items/{id} : Delete an order item.
     *
     * @param id the id of the order item to delete
     * @return the ResponseEntity with status 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order item", description = "Deletes the order item with the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order item successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Order item not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteOrderItem(
            @Parameter(description = "ID of the order item to delete", required = true)
            @PathVariable Long id) {
        orderItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 