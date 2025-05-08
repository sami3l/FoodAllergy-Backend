package com.sour.Backend_foodAllergy.service;

import com.sour.Backend_foodAllergy.dto.ScanRequest;
import com.sour.Backend_foodAllergy.dto.ScanResponse;
import com.sour.Backend_foodAllergy.dto.ProductInfoResponse;
import com.sour.Backend_foodAllergy.model.ProductScan;
import com.sour.Backend_foodAllergy.model.User;
import com.sour.Backend_foodAllergy.repository.ProductScanRepository;
import com.sour.Backend_foodAllergy.repository.UserRepository;
import com.sour.Backend_foodAllergy.utils.OpenFoodFactsClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductScanService {

    private final ProductScanRepository productScanRepository;
    private final UserRepository userRepository;
    private final AllergyService allergyService;
    private final OpenFoodFactsClient openFoodFactsClient;

    public ProductScanService(ProductScanRepository productScanRepository,
                              UserRepository userRepository,
                              AllergyService allergyService,
                              OpenFoodFactsClient openFoodFactsClient) {
        this.productScanRepository = productScanRepository;
        this.userRepository = userRepository;
        this.allergyService = allergyService;
        this.openFoodFactsClient = openFoodFactsClient;
    }

    public ScanResponse scanProduct(ScanRequest request) {
        // Step 1: Get user
        User user = userRepository.findById(Long.valueOf(request.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Get product info if barcode is provided (including image URL)
        String imageUrl = null;
        if (request.getBarcode() != null && !request.getBarcode().isEmpty()) {
            ProductInfoResponse productInfo = openFoodFactsClient.getProductInfo(request.getBarcode());
            if (productInfo != null && productInfo.getImageUrl() != null) {
                imageUrl = productInfo.getImageUrl();
            }
        }

        // Step 3: Detect allergens using NLP-based AllergyService
        String productText = request.getProductText() != null ? request.getProductText() : "";
        String userAllergyStr = user.getAllergies().toString();
        List<String> matchedAllergens = allergyService.detectAllergens(productText, userAllergyStr);

        // Step 4: Estimate risk level
        String riskLevel = allergyService.estimateRisk(matchedAllergens);

        // Step 5: Create and store scan record
        ProductScan scan = new ProductScan();
        scan.setUserId(Long.valueOf(request.getUserId()));
        scan.setProductText(productText);
        scan.setDetectedAllergens(matchedAllergens);
        scan.setRiskLevel(riskLevel);
        scan.setProductName(request.getProductName());
        scan.setBarcode(request.getBarcode());
        scan.setSource(request.getBarcode() != null && !request.getBarcode().isEmpty() ? "API" : "OCR");
        scan.setImageUrl(imageUrl);  // Set the image URL directly from API
        scan.setCreatedAt(LocalDateTime.now());

        productScanRepository.save(scan);

        // Step 6: Return structured response
        return new ScanResponse(
                matchedAllergens,
                riskLevel,
                scan.getProductName(),
                scan.getImageUrl(),
                scan.getSource(),
                scan.getCreatedAt()
        );
    }

    public Page<ProductScan> getScansByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productScanRepository.findByUserId(userId, pageable);
    }
}