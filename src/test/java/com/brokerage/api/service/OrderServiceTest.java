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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private AssetRepository assetRepository;
    
    @InjectMocks
    private OrderService orderService;
    
    private CreateOrderRequest buyOrderRequest;
    private CreateOrderRequest sellOrderRequest;
    private Asset tryAsset;
    private Asset stockAsset;
    private Order order;
    
    @BeforeEach
    void setUp() {
        buyOrderRequest = new CreateOrderRequest();
        buyOrderRequest.setCustomerId(1L);
        buyOrderRequest.setAssetName("AAPL");
        buyOrderRequest.setOrderSide(OrderSide.BUY);
        buyOrderRequest.setSize(new BigDecimal("10"));
        buyOrderRequest.setPrice(new BigDecimal("150.00"));
        
        sellOrderRequest = new CreateOrderRequest();
        sellOrderRequest.setCustomerId(1L);
        sellOrderRequest.setAssetName("AAPL");
        sellOrderRequest.setOrderSide(OrderSide.SELL);
        sellOrderRequest.setSize(new BigDecimal("5"));
        sellOrderRequest.setPrice(new BigDecimal("160.00"));
        
        tryAsset = new Asset();
        tryAsset.setId(1L);
        tryAsset.setCustomerId(1L);
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(new BigDecimal("10000.00"));
        tryAsset.setUsableSize(new BigDecimal("10000.00"));
        
        stockAsset = new Asset();
        stockAsset.setId(2L);
        stockAsset.setCustomerId(1L);
        stockAsset.setAssetName("AAPL");
        stockAsset.setSize(new BigDecimal("100"));
        stockAsset.setUsableSize(new BigDecimal("100"));
        
        order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setAssetName("AAPL");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(new BigDecimal("10"));
        order.setPrice(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
    }
    
    @Test
    void createOrder_BuyOrder_Success() {
        // Given
        when(assetRepository.findAssetByCustomerAndName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(assetRepository.save(any(Asset.class))).thenReturn(tryAsset);
        
        // When
        OrderResponse response = orderService.createOrder(buyOrderRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    void createOrder_SellOrder_Success() {
        // Given
        when(assetRepository.findAssetByCustomerAndName(1L, "AAPL"))
                .thenReturn(Optional.of(stockAsset));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(assetRepository.save(any(Asset.class))).thenReturn(stockAsset);
        
        // When
        OrderResponse response = orderService.createOrder(sellOrderRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    void createOrder_InsufficientTRYBalance_ThrowsException() {
        // Given
        tryAsset.setUsableSize(new BigDecimal("100.00")); // Not enough for 10 * 150 = 1500
        when(assetRepository.findAssetByCustomerAndName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        
        // When & Then
        assertThrows(InsufficientFundsException.class, () -> orderService.createOrder(buyOrderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void createOrder_InsufficientStockBalance_ThrowsException() {
        // Given
        stockAsset.setUsableSize(new BigDecimal("3")); // Not enough for 5
        when(assetRepository.findAssetByCustomerAndName(1L, "AAPL"))
                .thenReturn(Optional.of(stockAsset));
        
        // When & Then
        assertThrows(InsufficientFundsException.class, () -> orderService.createOrder(sellOrderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void createOrder_TryAsset_ThrowsException() {
        // Given
        CreateOrderRequest tryRequest = new CreateOrderRequest();
        tryRequest.setCustomerId(1L);
        tryRequest.setAssetName("TRY");
        tryRequest.setOrderSide(OrderSide.BUY);
        tryRequest.setSize(new BigDecimal("100"));
        tryRequest.setPrice(new BigDecimal("1.00"));
        
        // When & Then
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(tryRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void createOrder_InvalidSize_ThrowsException() {
        // Given
        buyOrderRequest.setSize(BigDecimal.ZERO);
        
        // When & Then
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(buyOrderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void createOrder_InvalidPrice_ThrowsException() {
        // Given
        buyOrderRequest.setPrice(BigDecimal.ZERO);
        
        // When & Then
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(buyOrderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void listOrders_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Order> orders = Arrays.asList(order);
        
        when(orderRepository.findOrdersByCustomerAndDateRange(1L, startDate, endDate))
                .thenReturn(orders);
        
        // When
        List<OrderResponse> responses = orderService.listOrders(1L, startDate, endDate);
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        verify(orderRepository).findOrdersByCustomerAndDateRange(1L, startDate, endDate);
    }
    
    @Test
    void deleteOrder_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(assetRepository.findAssetByCustomerAndName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(tryAsset);
        
        // When
        orderService.deleteOrder(1L, 1L);
        
        // Then
        verify(orderRepository).save(any(Order.class));
        verify(assetRepository).save(any(Asset.class));
    }
    
    @Test
    void deleteOrder_OrderNotFound_ThrowsException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(1L, 1L));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void deleteOrder_NotPendingStatus_ThrowsException() {
        // Given
        order.setStatus(OrderStatus.MATCHED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        // When & Then
        assertThrows(InvalidOrderException.class, () -> orderService.deleteOrder(1L, 1L));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void deleteOrder_NotCustomerOrder_ThrowsException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        // When & Then
        assertThrows(InvalidOrderException.class, () -> orderService.deleteOrder(1L, 2L));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void matchOrder_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(assetRepository.findAssetByCustomerAndName(1L, "AAPL"))
                .thenReturn(Optional.of(stockAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(stockAsset);
        
        // When
        OrderResponse response = orderService.matchOrder(1L);
        
        // Then
        assertNotNull(response);
        assertEquals(OrderStatus.MATCHED, response.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(assetRepository, atLeastOnce()).save(any(Asset.class));
    }
    
    @Test
    void matchOrder_OrderNotFound_ThrowsException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderService.matchOrder(1L));
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void matchOrder_NotPendingStatus_ThrowsException() {
        // Given
        order.setStatus(OrderStatus.MATCHED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        // When & Then
        assertThrows(InvalidOrderException.class, () -> orderService.matchOrder(1L));
        verify(orderRepository, never()).save(any(Order.class));
    }
}
