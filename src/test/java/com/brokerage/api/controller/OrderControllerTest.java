package com.brokerage.api.controller;

import com.brokerage.api.dto.CreateOrderRequest;
import com.brokerage.api.dto.MatchOrderRequest;
import com.brokerage.api.dto.OrderResponse;
import com.brokerage.api.model.Customer;
import com.brokerage.api.model.OrderSide;
import com.brokerage.api.service.CustomerService;
import com.brokerage.api.service.OrderService;
import com.brokerage.api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(com.brokerage.api.config.SecurityConfig.class)
class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private OrderService orderService;
    
    @MockBean
    private CustomerService customerService;
    
    @MockBean
    private JwtService jwtService;

    @MockBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private CreateOrderRequest createOrderRequest;
    private OrderResponse orderResponse;
    private Customer customer;
    private Customer adminCustomer;
    
    @BeforeEach
    void setUp() {
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerId(1L);
        createOrderRequest.setAssetName("AAPL");
        createOrderRequest.setOrderSide(OrderSide.BUY);
        createOrderRequest.setSize(new BigDecimal("10"));
        createOrderRequest.setPrice(new BigDecimal("150.00"));
        
        orderResponse = new OrderResponse();
        orderResponse.setId(1L);
        orderResponse.setCustomerId(1L);
        orderResponse.setAssetName("AAPL");
        orderResponse.setOrderSide(OrderSide.BUY);
        orderResponse.setSize(new BigDecimal("10"));
        orderResponse.setPrice(new BigDecimal("150.00"));
        orderResponse.setStatus(com.brokerage.api.model.OrderStatus.PENDING);
        orderResponse.setCreateDate(LocalDateTime.now());
        
        customer = new Customer();
        customer.setId(1L);
        customer.setUsername("john.doe");
        customer.setAdmin(false);
        
        adminCustomer = new Customer();
        adminCustomer.setId(999L);
        adminCustomer.setUsername("admin");
        adminCustomer.setAdmin(true);
    }
    
    @Test
    @WithMockUser(username = "john.doe")
    void createOrder_Success() throws Exception {
        // Given
        when(customerService.getCustomerByUsername("john.doe")).thenReturn(customer);
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);
        
        // When & Then
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("BUY"));
        
        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }
    
    @Test
    @WithMockUser(username = "admin")
    void createOrder_AdminUser_Success() throws Exception {
        // Given
        when(customerService.getCustomerByUsername("admin")).thenReturn(adminCustomer);
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);
        
        // When & Then
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isOk());
        
        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }
    
    @Test
    @WithMockUser(username = "john.doe")
    void createOrder_UnauthorizedCustomer_Forbidden() throws Exception {
        // Given
        createOrderRequest.setCustomerId(2L); // Different customer
        when(customerService.getCustomerByUsername("john.doe")).thenReturn(customer);
        
        // When & Then
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isForbidden());
        
        verify(orderService, never()).createOrder(any(CreateOrderRequest.class));
    }
    
    @Test
    @WithMockUser(username = "john.doe")
    void listOrders_Success() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<OrderResponse> orders = Arrays.asList(orderResponse);
        
        when(customerService.getCustomerByUsername("john.doe")).thenReturn(customer);
        when(orderService.listOrders(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(orders);
        
        // When & Then
        mockMvc.perform(get("/orders")
                        .param("customerId", "1")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].assetName").value("AAPL"));
        
        verify(orderService).listOrders(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test
    @WithMockUser(username = "john.doe")
    void listOrders_UnauthorizedCustomer_Forbidden() throws Exception {
        // Given
        when(customerService.getCustomerByUsername("john.doe")).thenReturn(customer);
        
        // When & Then
        mockMvc.perform(get("/orders")
                        .param("customerId", "2") // Different customer
                        .param("startDate", LocalDateTime.now().minusDays(7).toString())
                        .param("endDate", LocalDateTime.now().toString()))
                .andExpect(status().isForbidden());
        
        verify(orderService, never()).listOrders(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "john.doe")
    void deleteOrder_Success() throws Exception {
        // Given
        when(customerService.getCustomerByUsername("john.doe")).thenReturn(customer);
        doNothing().when(orderService).deleteOrder(1L, 1L);
        
        // When & Then
        mockMvc.perform(delete("/orders/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(orderService).deleteOrder(1L, 1L);
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void matchOrder_AdminUser_Success() throws Exception {
        // Given
        MatchOrderRequest matchRequest = new MatchOrderRequest();
        matchRequest.setOrderId(1L);
        
        when(orderService.matchOrder(1L)).thenReturn(orderResponse);
        
        // When & Then
        mockMvc.perform(post("/orders/1/match")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(matchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(orderService).matchOrder(1L);
    }
    
    @Test
    @WithMockUser(username = "john.doe")
    void matchOrder_NonAdminUser_Forbidden() throws Exception {
        // Given
        MatchOrderRequest matchRequest = new MatchOrderRequest();
        matchRequest.setOrderId(1L);
        
        // When & Then
        mockMvc.perform(post("/orders/1/match")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(matchRequest)))
                .andExpect(status().isForbidden());
        
        verify(orderService, never()).matchOrder(any());
    }
}
