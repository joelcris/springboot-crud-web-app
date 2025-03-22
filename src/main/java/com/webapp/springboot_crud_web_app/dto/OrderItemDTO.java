package com.webapp.springboot_crud_web_app.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    
    @Schema(accessMode = AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(accessMode = AccessMode.READ_ONLY)
    private Long orderId;
    
    @Schema(example = "1")
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Schema(example = "2")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @Schema(example = "19.99")
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Unit price must be greater than 0.00")
    private BigDecimal unitPrice;
    
    @Schema(example = "39.98")
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Subtotal must be greater than or equal to 0.00")
    private BigDecimal subtotal;
} 