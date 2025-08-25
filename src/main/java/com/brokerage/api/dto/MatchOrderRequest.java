package com.brokerage.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchOrderRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    // Additional fields for order matching could be added here in the future
    // For example: matching price, partial matching, etc.
}
