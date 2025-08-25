package com.brokerage.api.service;

import com.brokerage.api.dto.LoginRequest;
import com.brokerage.api.dto.LoginResponse;
import com.brokerage.api.exception.AuthenticationException;
import com.brokerage.api.exception.CustomerNotFoundException;
import com.brokerage.api.model.Customer;
import com.brokerage.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }
        
        String token = jwtService.generateToken(customer.getUsername(), customer.isAdmin());
        
        log.info("User {} logged in successfully", request.getUsername());
        
        return new LoginResponse(token, customer.getUsername(), customer.isAdmin(), "Login successful");
    }
    
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
    }
    
    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with username: " + username));
    }
}
