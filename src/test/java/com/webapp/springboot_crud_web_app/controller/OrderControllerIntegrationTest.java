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
import com.webapp.springboot_crud_web_app.service.OrderService;
import com.webapp.springboot_crud_web_app.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    private ProductDTO product1;
    private ProductDTO product2;
    
    @BeforeEach
    void setUp() {
        // Create test products programmatically
        ProductDTO productDTO1 = ProductDTO.builder()
                .name("Test Product 1")
                .description("Description for test product 1")
                .price(BigDecimal.valueOf(19.99))
                .stock(100)
                .build();
        
        ProductDTO productDTO2 = ProductDTO.builder()
                .name("Test Product 2")
                .description("Description for test product 2")
                .price(BigDecimal.valueOf(49.99))
                .stock(50)
                .build();
        
        product1 = productService.create(productDTO1);
        product2 = productService.create(productDTO2);
    }

    @Test
    void createOrder_ValidInput_ReturnsCreatedOrder() throws Exception {
        OrderItemDTO orderItemDTO1 = OrderItemDTO.builder()
                .productId(product1.getId())
                .quantity(2)
                .unitPrice(product1.getPrice())
                .subtotal(product1.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();

        OrderItemDTO orderItemDTO2 = OrderItemDTO.builder()
                .productId(product2.getId())
                .quantity(1)
                .unitPrice(product2.getPrice())
                .subtotal(product2.getPrice())
                .build();

        BigDecimal totalAmount = orderItemDTO1.getSubtotal().add(orderItemDTO2.getSubtotal());
        
        OrderDTO orderDTO = OrderDTO.builder()
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .shippingAddress("123 Test Street")
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(orderItemDTO1, orderItemDTO2))
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andDo(MockMvcResultHandlers.print()) // Print request/response for debugging
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerName").value("Test Customer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerEmail").value("test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.shippingAddress").value("123 Test Street"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalAmount").value(totalAmount.doubleValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].productId").value(product1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].quantity").value(2));
    }
    
    @Test
    void getAllOrders_ReturnsOrdersList() throws Exception {
        // Create an order first to ensure there's at least one order in the system
        createTestOrder();
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].customerName").exists());
    }
    
    @Test
    void getOrderById_ExistingOrder_ReturnsOrder() throws Exception {
        // Create an order and get its ID
        OrderDTO createdOrder = createTestOrder();
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/{id}", createdOrder.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdOrder.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerName").value("Test Customer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerEmail").value("test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems").isArray());
    }
    
    @Test
    void getOrderById_NonExistingOrder_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void updateOrder_ValidInput_ReturnsUpdatedOrder() throws Exception {
        // Create an order first
        OrderDTO createdOrder = createTestOrder();
        
        // Prepare update data
        createdOrder.setCustomerName("Updated Customer");
        createdOrder.setStatus(OrderStatus.SHIPPED);
        
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{id}", createdOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdOrder)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdOrder.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerName").value("Updated Customer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SHIPPED"));
    }
    
    @Test
    void updateOrder_NonExistingOrder_ReturnsNotFound() throws Exception {
        OrderDTO orderDTO = createTestOrderDTO();
        
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void deleteOrder_ExistingOrder_ReturnsNoContent() throws Exception {
        // Create an order first
        OrderDTO createdOrder = createTestOrder();
        
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/orders/{id}", createdOrder.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        
        // Verify the order is deleted
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/{id}", createdOrder.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void deleteOrder_NonExistingOrder_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/orders/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    
    @Test
    void createOrder_InvalidEmail_ReturnsBadRequest() throws Exception {
        OrderDTO orderDTO = createTestOrderDTO();
        orderDTO.setCustomerEmail("invalid-email"); // Invalid email format
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'customerEmail')]").exists());
    }
    
    @Test
    void createOrder_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        OrderDTO orderDTO = new OrderDTO(); // Empty DTO with missing required fields
        orderDTO.setOrderItems(Arrays.asList()); // Empty order items list to avoid null
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'customerName')]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'customerEmail')]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'shippingAddress')]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'totalAmount')]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'status')]").exists());
    }
    
    @Test
    void createOrder_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Create a product with limited stock
        ProductDTO lowStockProduct = ProductDTO.builder()
                .name("Limited Stock Product")
                .description("Product with limited stock")
                .price(BigDecimal.valueOf(29.99))
                .stock(5) // Only 5 in stock
                .build();
        
        ProductDTO createdProduct = productService.create(lowStockProduct);
        
        // Try to order more than available stock
        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .productId(createdProduct.getId())
                .quantity(10) // Requesting more than available
                .unitPrice(createdProduct.getPrice())
                .subtotal(createdProduct.getPrice().multiply(BigDecimal.valueOf(10)))
                .build();
        
        OrderDTO orderDTO = OrderDTO.builder()
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .shippingAddress("123 Test Street")
                .totalAmount(orderItemDTO.getSubtotal())
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(orderItemDTO))
                .build();
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("stock")));
    }
    
    @Test
    void createOrder_ZeroQuantity_ReturnsBadRequest() throws Exception {
        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .productId(product1.getId())
                .quantity(0) // Zero quantity - should be rejected
                .unitPrice(product1.getPrice())
                .subtotal(BigDecimal.ZERO)
                .build();
        
        OrderDTO orderDTO = OrderDTO.builder()
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .shippingAddress("123 Test Street")
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(orderItemDTO))
                .build();
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'orderItems[0].quantity')]").exists());
    }
    
    @Test
    void createOrder_VeryLargeOrder_ReturnsCreatedOrder() throws Exception {
        // Create a large order with 10 items of the same product
        int largeQuantity = 50; // Large but within stock limits
        
        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .productId(product1.getId())
                .quantity(largeQuantity)
                .unitPrice(product1.getPrice())
                .subtotal(product1.getPrice().multiply(BigDecimal.valueOf(largeQuantity)))
                .build();
        
        OrderDTO orderDTO = OrderDTO.builder()
                .customerName("Bulk Buyer")
                .customerEmail("bulk@example.com")
                .shippingAddress("456 Warehouse Ave")
                .totalAmount(orderItemDTO.getSubtotal())
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(orderItemDTO))
                .build();
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerName").value("Bulk Buyer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderItems[0].quantity").value(largeQuantity));
        
        // Verify the stock was updated correctly
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/{id}", product1.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.stock").value(100 - largeQuantity)); // Check stock reduction
    }
    
    @Test
    void createOrder_NegativeQuantity_ReturnsBadRequest() throws Exception {
        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .productId(product1.getId())
                .quantity(-5) // Negative quantity - should be rejected
                .unitPrice(product1.getPrice())
                .subtotal(product1.getPrice().multiply(BigDecimal.valueOf(-5)))
                .build();
        
        OrderDTO orderDTO = OrderDTO.builder()
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .shippingAddress("123 Test Street")
                .totalAmount(orderItemDTO.getSubtotal())
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(orderItemDTO))
                .build();
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[?(@.field == 'orderItems[0].quantity')]").exists());
    }
    
    /**
     * Helper method to create a test order DTO without saving it
     */
    private OrderDTO createTestOrderDTO() {
        OrderItemDTO orderItemDTO1 = OrderItemDTO.builder()
                .productId(product1.getId())
                .quantity(2)
                .unitPrice(product1.getPrice())
                .subtotal(product1.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();

        OrderItemDTO orderItemDTO2 = OrderItemDTO.builder()
                .productId(product2.getId())
                .quantity(1)
                .unitPrice(product2.getPrice())
                .subtotal(product2.getPrice())
                .build();
        
        BigDecimal totalAmount = orderItemDTO1.getSubtotal().add(orderItemDTO2.getSubtotal());

        return OrderDTO.builder()
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .shippingAddress("123 Test Street")
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .orderItems(Arrays.asList(orderItemDTO1, orderItemDTO2))
                .build();
    }
    
    /**
     * Helper method to create and save a test order
     */
    private OrderDTO createTestOrder() {
        OrderDTO orderDTO = createTestOrderDTO();
        return orderService.create(orderDTO);
    }
} 