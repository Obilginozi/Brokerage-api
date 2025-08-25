package com.brokerage.api.dto;

import com.brokerage.api.model.OrderSide;
import com.brokerage.api.model.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private Long customerId;
    private String assetName;
    private OrderSide orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createDate;
}
