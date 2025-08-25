package com.brokerage.api.controller;

import com.brokerage.api.dto.CreateOrderRequest;
import com.brokerage.api.dto.MatchOrderRequest;
import com.brokerage.api.dto.OrderResponse;
import com.brokerage.api.service.CustomerService;
import com.brokerage.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    private final CustomerService customerService;
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                   Authentication authentication) {
        log.info("Create order request received for customer: {}", request.getCustomerId());
        
        // Check if user is admin or the order belongs to the authenticated user
        String username = authentication.getName();
        var customer = customerService.getCustomerByUsername(username);
        
        if (!customer.isAdmin() && !customer.getId().equals(request.getCustomerId())) {
            return ResponseEntity.status(403).build();
        }
        
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        
        log.info("List orders request received for customer: {} between {} and {}", 
                customerId, startDate, endDate);
        
        // Check if the user is admin or the request is for the authenticated user
        String username = authentication.getName();
        var customer = customerService.getCustomerByUsername(username);
        
        if (!customer.isAdmin() && !customer.getId().equals(customerId)) {
            return ResponseEntity.status(403).build();
        }
        
        List<OrderResponse> orders = orderService.listOrders(customerId, startDate, endDate);
        return ResponseEntity.ok(orders);
    }
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId,
                                          Authentication authentication) {
        log.info("Delete order request received for order: {}", orderId);
        
        String username = authentication.getName();
        var customer = customerService.getCustomerByUsername(username);
        
        orderService.deleteOrder(orderId, customer.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{orderId}/match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> matchOrder(@PathVariable Long orderId,
                                                  @Valid @RequestBody MatchOrderRequest request) {
        log.info("Match order request received for order: {}", orderId);
        
        OrderResponse response = orderService.matchOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
