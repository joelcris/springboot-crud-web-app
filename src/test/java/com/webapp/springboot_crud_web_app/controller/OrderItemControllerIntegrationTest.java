package com.webapp.springboot_crud_web_app.controller;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webapp.springboot_crud_web_app.dto.OrderDTO;
import com.webapp.springboot_crud_web_app.dto.OrderItemDTO;
import com.webapp.springboot_crud_web_app.dto.ProductDTO;
import com.webapp.springboot_crud_web_app.model.Order.OrderStatus;
import com.webapp.springboot_crud_web_app.service.OrderItemService;
import com.webapp.springboot_crud_web_app.service.OrderService;
import com.webapp.springboot_crud_web_app.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class OrderItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private OrderItemService orderItemService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    private ProductDTO product;
    private OrderDTO order;
    
    @BeforeEach
    void setUp() {
        // Create test product
        ProductDTO productDTO = ProductDTO.builder()
                .name("Test Product")
                .description("Description for test product")
                .price(BigDecimal.valueOf(19.99))
                .stock(100)
                .build();
        
        product = productService.create(productDTO);
        
        // Create an initial OrderItemDTO that will be needed for order creation
        OrderItemDTO initialItemDTO = OrderItemDTO.builder()
                .productId(product.getId())
                .quantity(1)
                .unitPrice(product.getPrice())
                .subtotal(product.getPrice())
                .build();
        
        // Create test order with the initial item
        OrderDTO orderDTO = OrderDTO.builder()
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .shippingAddress("123 Test Street")
                .totalAmount(initialItemDTO.getSubtotal())
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(initialItemDTO))
                .build();
        
        order = orderService.create(orderDTO);
    }

    @Test
    void createOrderItem_ValidInput_ReturnsCreatedOrderItem() throws Exception {
        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .orderId(order.getId())
                .productId(product.getId())
                .quantity(2)
                .unitPrice(product.getPrice())
                .subtotal(product.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(order.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(product.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.unitPrice").value(product.getPrice().doubleValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.subtotal").value(product.getPrice().multiply(BigDecimal.valueOf(2)).doubleValue()));
    }
    
    @Test
    void getAllOrderItems_ReturnsOrderItemsList() throws Exception {
        // Create an order item first to ensure there's at least one in the system
        createTestOrderItem();
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/order-items")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].orderId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].productId").exists());
    }
    
    @Test
    void getOrderItemById_ExistingOrderItem_ReturnsOrderItem() throws Exception {
        // Create an order item and get its ID
        OrderItemDTO createdOrderItem = createTestOrderItem();
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/order-items/{id}", createdOrderItem.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdOrderItem.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(order.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(product.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(createdOrderItem.getQuantity()));
    }
    
    @Test
    void getOrderItemById_NonExistingOrderItem_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/order-items/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void updateOrderItem_ValidInput_ReturnsUpdatedOrderItem() throws Exception {
        // Create an order item first
        OrderItemDTO createdOrderItem = createTestOrderItem();
        
        // Prepare update data
        createdOrderItem.setQuantity(5);
        createdOrderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(5)));
        
        mockMvc.perform(MockMvcRequestBuilders.put("/api/order-items/{id}", createdOrderItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdOrderItem)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdOrderItem.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.subtotal").value(product.getPrice().multiply(BigDecimal.valueOf(5)).doubleValue()));
    }
    
    @Test
    void updateOrderItem_NonExistingOrderItem_ReturnsNotFound() throws Exception {
        OrderItemDTO orderItemDTO = createTestOrderItemDTO();
        
        mockMvc.perform(MockMvcRequestBuilders.put("/api/order-items/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderItemDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void deleteOrderItem_ExistingOrderItem_ReturnsNoContent() throws Exception {
        // Create an order item first
        OrderItemDTO createdOrderItem = createTestOrderItem();
        
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/order-items/{id}", createdOrderItem.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        // Verify the order item is deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/order-items/{id}", createdOrderItem.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void deleteOrderItem_NonExistingOrderItem_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/order-items/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void createOrderItem_InvalidQuantity_ReturnsBadRequest() throws Exception {
        OrderItemDTO orderItemDTO = createTestOrderItemDTO();
        orderItemDTO.setQuantity(0); // Zero quantity - should be rejected
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/order-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderItemDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'quantity')]").exists());
    }
    
    @Test
    void createOrderItem_NegativeQuantity_ReturnsBadRequest() throws Exception {
        OrderItemDTO orderItemDTO = createTestOrderItemDTO();
        orderItemDTO.setQuantity(-5); // Negative quantity - should be rejected
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/order-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderItemDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'quantity')]").exists());
    }
    
    @Test
    void createOrderItem_InvalidOrderId_ReturnsNotFound() throws Exception {
        OrderItemDTO orderItemDTO = createTestOrderItemDTO();
        orderItemDTO.setOrderId(999999L); // Non-existent order ID
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/order-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderItemDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Expect 404 Not Found
    }
    
    @Test
    void createOrderItem_InvalidProductId_ReturnsNotFound() throws Exception {
        OrderItemDTO orderItemDTO = createTestOrderItemDTO();
        orderItemDTO.setProductId(999999L); // Non-existent product ID
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/order-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderItemDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Expect 404 Not Found
    }
    
    /**
     * Helper method to create a test order item DTO without saving it
     */
    private OrderItemDTO createTestOrderItemDTO() {
        return OrderItemDTO.builder()
                .orderId(order.getId())
                .productId(product.getId())
                .quantity(2)
                .unitPrice(product.getPrice())
                .subtotal(product.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();
    }
    
    /**
     * Helper method to create and save a test order item
     */
    private OrderItemDTO createTestOrderItem() {
        OrderItemDTO orderItemDTO = createTestOrderItemDTO();
        return orderItemService.create(orderItemDTO, orderItemDTO.getOrderId(), orderItemDTO.getProductId());
    }
} 