package com.brokerage.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "asset_name", nullable = false)
    private String assetName;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal size;
    
    @Column(name = "usable_size", nullable = false, precision = 19, scale = 4)
    private BigDecimal usableSize;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
}
