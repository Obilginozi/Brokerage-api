package com.brokerage.api.controller;

import com.brokerage.api.dto.AssetResponse;
import com.brokerage.api.service.AssetService;
import com.brokerage.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetController {
    
    private final AssetService assetService;
    private final CustomerService customerService;
    
    @GetMapping
    public ResponseEntity<List<AssetResponse>> listAssets(@RequestParam Long customerId,
                                                        Authentication authentication) {
        log.info("List assets request received for customer: {}", customerId);
        
        // Check if user is admin or the request is for the authenticated user
        String username = authentication.getName();
        var customer = customerService.getCustomerByUsername(username);
        
        if (!customer.isAdmin() && !customer.getId().equals(customerId)) {
            return ResponseEntity.status(403).build();
        }
        
        List<AssetResponse> assets = assetService.listAssets(customerId);
        return ResponseEntity.ok(assets);
    }
}
