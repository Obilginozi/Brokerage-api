package com.brokerage.api.service;

import com.brokerage.api.dto.CreateOrderRequest;
import com.brokerage.api.dto.OrderResponse;
import com.brokerage.api.exception.InsufficientFundsException;
import com.brokerage.api.exception.InvalidOrderException;
import com.brokerage.api.exception.OrderNotFoundException;
import com.brokerage.api.model.Asset;
import com.brokerage.api.model.Order;
import com.brokerage.api.model.OrderSide;
import com.brokerage.api.model.OrderStatus;
import com.brokerage.api.repository.AssetRepository;
import com.brokerage.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    
    private static final String TRY_ASSET = "TRY";
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}, asset: {}, side: {}, size: {}, price: {}", 
                request.getCustomerId(), request.getAssetName(), request.getOrderSide(), 
                request.getSize(), request.getPrice());
        
        // Validate order
        validateOrder(request);
        
        // Check if customer has sufficient funds/assets
        checkSufficientFunds(request);
        
        // Create and save order
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setAssetName(request.getAssetName());
        order.setOrderSide(request.getOrderSide());
        order.setSize(request.getSize());
        order.setPrice(request.getPrice());
        order.setStatus(OrderStatus.PENDING);
        
        Order savedOrder = orderRepository.save(order);
        
        // Update asset balances
        updateAssetBalances(request);
        
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return mapToOrderResponse(savedOrder);
    }
    
    public List<OrderResponse> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Listing orders for customer: {} between {} and {}", customerId, startDate, endDate);
        
        List<Order> orders = orderRepository.findOrdersByCustomerAndDateRange(customerId, startDate, endDate);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteOrder(Long orderId, Long customerId) {
        log.info("Deleting order: {} for customer: {}", orderId, customerId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
        
        // Check if order belongs to customer or if customer is admin
        if (!order.getCustomerId().equals(customerId)) {
            throw new InvalidOrderException("Order does not belong to customer");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException("Only pending orders can be deleted");
        }
        
        // Update order status
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        
        // Restore asset balances
        restoreAssetBalances(order);
        
        log.info("Order {} deleted successfully", orderId);
    }
    
    @Transactional
    public OrderResponse matchOrder(Long orderId) {
        log.info("Matching order: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException("Only pending orders can be matched");
        }
        
        // Update order status
        order.setStatus(OrderStatus.MATCHED);
        Order savedOrder = orderRepository.save(order);
        
        // Update asset balances for matched order
        updateAssetBalancesForMatchedOrder(order);
        
        log.info("Order {} matched successfully", orderId);
        return mapToOrderResponse(savedOrder);
    }
    
    private void validateOrder(CreateOrderRequest request) {
        if (request.getSize().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("Order size must be greater than 0");
        }
        
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("Order price must be greater than 0");
        }
        
        if (request.getAssetName().equals(TRY_ASSET)) {
            throw new InvalidOrderException("Cannot trade TRY asset directly");
        }
    }
    
    private void checkSufficientFunds(CreateOrderRequest request) {
        if (request.getOrderSide() == OrderSide.BUY) {
            // Check if customer has enough TRY
            Asset tryAsset = assetRepository.findAssetByCustomerAndName(request.getCustomerId(), TRY_ASSET)
                    .orElseThrow(() -> new InvalidOrderException("TRY asset not found for customer"));
            
            BigDecimal requiredAmount = request.getSize().multiply(request.getPrice());
            if (tryAsset.getUsableSize().compareTo(requiredAmount) < 0) {
                throw new InsufficientFundsException("Insufficient TRY balance. Required: " + requiredAmount + ", Available: " + tryAsset.getUsableSize());
            }
        } else {
            // Check if customer has enough of the asset to sell
            Asset asset = assetRepository.findAssetByCustomerAndName(request.getCustomerId(), request.getAssetName())
                    .orElseThrow(() -> new InvalidOrderException("Asset not found: " + request.getAssetName()));
            
            if (asset.getUsableSize().compareTo(request.getSize()) < 0) {
                throw new InsufficientFundsException("Insufficient asset balance. Required: " + request.getSize() + ", Available: " + asset.getUsableSize());
            }
        }
    }
    
    private void updateAssetBalances(CreateOrderRequest request) {
        if (request.getOrderSide() == OrderSide.BUY) {
            // Reserve TRY for the order
            Asset tryAsset = assetRepository.findAssetByCustomerAndName(request.getCustomerId(), TRY_ASSET).get();
            BigDecimal requiredAmount = request.getSize().multiply(request.getPrice());
            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(requiredAmount));
            assetRepository.save(tryAsset);
        } else {
            // Reserve the asset being sold
            Asset asset = assetRepository.findAssetByCustomerAndName(request.getCustomerId(), request.getAssetName()).get();
            asset.setUsableSize(asset.getUsableSize().subtract(request.getSize()));
            assetRepository.save(asset);
        }
    }
    
    private void restoreAssetBalances(Order order) {
        if (order.getOrderSide() == OrderSide.BUY) {
            // Restore TRY balance
            Asset tryAsset = assetRepository.findAssetByCustomerAndName(order.getCustomerId(), TRY_ASSET).get();
            BigDecimal amount = order.getSize().multiply(order.getPrice());
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(amount));
            assetRepository.save(tryAsset);
        } else {
            // Restore asset balance
            Asset asset = assetRepository.findAssetByCustomerAndName(order.getCustomerId(), order.getAssetName()).get();
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        }
    }
    
    private void updateAssetBalancesForMatchedOrder(Order order) {
        if (order.getOrderSide() == OrderSide.BUY) {
            // Customer bought asset, update both TRY and asset balances
            Asset asset = assetRepository.findAssetByCustomerAndName(order.getCustomerId(), order.getAssetName()).get();
            
            // TRY was already reserved during order creation, so no change needed
            // Add the bought asset
            if (asset == null) {
                // Create new asset if it doesn't exist
                asset = new Asset();
                asset.setCustomerId(order.getCustomerId());
                asset.setAssetName(order.getAssetName());
                asset.setSize(order.getSize());
                asset.setUsableSize(order.getSize());
            } else {
                asset.setSize(asset.getSize().add(order.getSize()));
                asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            }
            assetRepository.save(asset);
        } else {
            // Customer sold asset, add TRY to balance
            Asset tryAsset = assetRepository.findAssetByCustomerAndName(order.getCustomerId(), TRY_ASSET).get();
            BigDecimal amount = order.getSize().multiply(order.getPrice());
            tryAsset.setSize(tryAsset.getSize().add(amount));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(amount));
            assetRepository.save(tryAsset);
        }
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setAssetName(order.getAssetName());
        response.setOrderSide(order.getOrderSide());
        response.setSize(order.getSize());
        response.setPrice(order.getPrice());
        response.setStatus(order.getStatus());
        response.setCreateDate(order.getCreateDate());
        return response;
    }
}
