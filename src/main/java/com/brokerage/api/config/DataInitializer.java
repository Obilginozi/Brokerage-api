package com.brokerage.api.config;

import com.brokerage.api.model.Asset;
import com.brokerage.api.model.Customer;
import com.brokerage.api.repository.AssetRepository;
import com.brokerage.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing sample data...");
        
        // Create admin user
        if (!customerRepository.existsByUsername("admin")) {
            Customer admin = new Customer();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrator");
            admin.setEmail("admin@brokerage.com");
            admin.setAdmin(true);
            customerRepository.save(admin);
            log.info("Admin user created");
            
            // Create TRY asset for admin
            Asset adminTry = new Asset();
            adminTry.setCustomerId(admin.getId());
            adminTry.setAssetName("TRY");
            adminTry.setSize(new BigDecimal("1000000.00"));
            adminTry.setUsableSize(new BigDecimal("1000000.00"));
            assetRepository.save(adminTry);
        }
        
        // Create sample customers
        createSampleCustomer("john.doe", "John Doe", "john@example.com", "password123");
        createSampleCustomer("jane.smith", "Jane Smith", "jane@example.com", "password123");
        createSampleCustomer("bob.wilson", "Bob Wilson", "bob@example.com", "password123");
        
        log.info("Sample data initialization completed");
    }
    
    private void createSampleCustomer(String username, String fullName, String email, String password) {
        if (!customerRepository.existsByUsername(username)) {
            Customer customer = new Customer();
            customer.setUsername(username);
            customer.setPassword(passwordEncoder.encode(password));
            customer.setFullName(fullName);
            customer.setEmail(email);
            customer.setAdmin(false);
            customerRepository.save(customer);
            
            // Create TRY asset for customer
            Asset tryAsset = new Asset();
            tryAsset.setCustomerId(customer.getId());
            tryAsset.setAssetName("TRY");
            tryAsset.setSize(new BigDecimal("10000.00"));
            tryAsset.setUsableSize(new BigDecimal("10000.00"));
            assetRepository.save(tryAsset);
            
            // Create some sample stock assets
            Asset aaplAsset = new Asset();
            aaplAsset.setCustomerId(customer.getId());
            aaplAsset.setAssetName("AAPL");
            aaplAsset.setSize(new BigDecimal("100.00"));
            aaplAsset.setUsableSize(new BigDecimal("100.00"));
            assetRepository.save(aaplAsset);
            
            Asset googlAsset = new Asset();
            googlAsset.setCustomerId(customer.getId());
            googlAsset.setAssetName("GOOGL");
            googlAsset.setSize(new BigDecimal("50.00"));
            googlAsset.setUsableSize(new BigDecimal("50.00"));
            assetRepository.save(googlAsset);
            
            log.info("Sample customer {} created with assets", username);
        }
    }
}
