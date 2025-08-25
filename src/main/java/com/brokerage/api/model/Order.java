package com.brokerage.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "asset_name", nullable = false)
    private String assetName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false)
    private OrderSide orderSide;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal size;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
    
    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }
}
