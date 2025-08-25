package com.brokerage.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetResponse {
    private Long id;
    private Long customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
}
