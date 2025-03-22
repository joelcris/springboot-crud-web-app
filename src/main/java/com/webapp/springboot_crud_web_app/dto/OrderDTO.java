package com.webapp.springboot_crud_web_app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.webapp.springboot_crud_web_app.model.Order.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    
    @Schema(accessMode = AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(example = "John Doe")
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;
    
    @Schema(example = "john.doe@example.com")
    @NotBlank(message = "Customer email is required")
    @Email(message = "Email must be valid")
    private String customerEmail;
    
    @NotBlank(message = "Shipping address is required")
    @Size(min = 5, max = 500, message = "Shipping address must be between 5 and 500 characters")
    @Schema(example = "Main St, Anytown")
    private String shippingAddress;
    
    @Schema(example = "99.99")
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Total amount must be greater than or equal to 0.00")
    private BigDecimal totalAmount;
    
    @Schema(example = "PENDING")
    @NotNull(message = "Order status is required")
    private OrderStatus status;
    
    @Valid
    private List<OrderItemDTO> orderItems = new ArrayList<>();
    
    @Schema(accessMode = AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    @Schema(accessMode = AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
} 