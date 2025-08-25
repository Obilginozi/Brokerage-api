package com.brokerage.api.controller;

import com.brokerage.api.dto.LoginRequest;
import com.brokerage.api.dto.LoginResponse;
import com.brokerage.api.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final CustomerService customerService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());
        
        LoginResponse response = customerService.login(request);
        
        log.info("Login successful for user: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }
}
