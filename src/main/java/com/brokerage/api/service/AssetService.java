package com.brokerage.api.service;

import com.brokerage.api.dto.AssetResponse;
import com.brokerage.api.model.Asset;
import com.brokerage.api.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {
    
    private final AssetRepository assetRepository;
    
    public List<AssetResponse> listAssets(Long customerId) {
        log.info("Listing assets for customer: {}", customerId);
        
        List<Asset> assets = assetRepository.findByCustomerId(customerId);
        return assets.stream()
                .map(this::mapToAssetResponse)
                .collect(Collectors.toList());
    }
    
    private AssetResponse mapToAssetResponse(Asset asset) {
        AssetResponse response = new AssetResponse();
        response.setId(asset.getId());
        response.setCustomerId(asset.getCustomerId());
        response.setAssetName(asset.getAssetName());
        response.setSize(asset.getSize());
        response.setUsableSize(asset.getUsableSize());
        return response;
    }
}
