package com.sour.Backend_foodAllergy.service;

import com.sour.Backend_foodAllergy.dto.ScanRequest;
import com.sour.Backend_foodAllergy.dto.ScanResponse;
import com.sour.Backend_foodAllergy.dto.ProductInfoResponse;
import com.sour.Backend_foodAllergy.model.ProductScan;
import com.sour.Backend_foodAllergy.model.User;
import com.sour.Backend_foodAllergy.repository.ProductScanRepository;
import com.sour.Backend_foodAllergy.repository.UserRepository;
import com.sour.Backend_foodAllergy.utils.OpenFoodFactsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional  // pour garantir l’atomicité de la méthode
public class ProductScanService {

    private final ProductScanRepository productScanRepository;
    private final UserRepository userRepository;
    private final AllergyService allergyService;
    private final OpenFoodFactsClient openFoodFactsClient;

    public ScanResponse scanProduct(ScanRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ExceptionInInitializerError(
                        username));
        Set<String> userAllergySet = Optional.ofNullable(user.getAllergies())
                .orElse(List.of())
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // 1) Récupération des données OpenFoodFacts
        ProductInfoResponse info = null;
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            info = openFoodFactsClient.getProductInfo(request.getBarcode());
        }

        // 2) Construction de la liste d’ingrédients
        String ingredientsText = (info != null && info.getIngredients() != null)
                ? info.getIngredients()
                : Optional.ofNullable(request.getProductText()).orElse("");

        List<String> ingredients = extractIngredients(ingredientsText);

        // 3) Détection d’allergènes par liste
        List<String> matchedAllergens = allergyService
                .detectAllergensInIngredients(ingredients, userAllergySet);

        // 4) Estimation du risque
        String riskLevel = allergyService.estimateRisk(matchedAllergens);

        // 5) Persister le scan
        ProductScan scan = saveProductScan(request, username, ingredients,
                matchedAllergens, riskLevel, info);

        // 6) Retourner le DTO
        return toScanResponse(scan);
    }

    private List<String> extractIngredients(String ingredientsText) {
        return Arrays.stream(ingredientsText
                        .toLowerCase()
                        .split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // méthode auxiliaire
    private String fetchImageUrl(String barcode) {
        if (barcode==null || barcode.isBlank()) return null;
        ProductInfoResponse info = openFoodFactsClient.getProductInfo(barcode);
        return Optional.ofNullable(info).map(ProductInfoResponse::getImageUrl).orElse(null);
    }

    private ProductScan saveProductScan(ScanRequest req,
                                        List<String> matchedAllergens,
                                        String riskLevel,
                                        String imageUrl) {
        ProductScan scan = new ProductScan();
        scan.setProductName(req.getProductName());
        scan.setBarcode(req.getBarcode());
        scan.setProductText(Optional.ofNullable(req.getProductText()).orElse(""));
        scan.setDetectedAllergens(matchedAllergens);
        scan.setRiskLevel(riskLevel);
        scan.setSource((req.getBarcode() != null && !req.getBarcode().isBlank()) ? "API" : "OCR");
        scan.setImageUrl(imageUrl);
        scan.setCreatedAt(LocalDateTime.now());
        scan.setStatus("Evaluated");

        return productScanRepository.save(scan);
    }

    private ScanResponse toScanResponse(ProductScan scan) {
        return new ScanResponse(
                scan.getDetectedAllergens(),
                scan.getRiskLevel(),
                scan.getProductName(),
                scan.getImageUrl(),
                scan.getSource(),
                scan.getCreatedAt()
        );
    }

    // Pagination des scans d’un utilisateur
    public Page<ProductScan> getScansByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productScanRepository.findByUserId(userId, pageable);
    }

    private ProductScan saveProductScan(
            ScanRequest req,
            String username,
            List<String> ingredients,
            List<String> matchedAllergens,
            String riskLevel,
            ProductInfoResponse info
    ) {
        // Reconstruis ici le texte du produit depuis la liste d’ingrédients
        String productText = String.join(", ", ingredients);

        // Récupère l’URL de l’image si elle existe
        String imageUrl = info != null ? info.getImageUrl() : null;

        ProductScan scan = new ProductScan();
        scan.setUserId(username);               // ou user.getId().toString()
        scan.setProductName(req.getProductName());
        scan.setBarcode(req.getBarcode());
        scan.setProductText(productText);
        scan.setDetectedAllergens(matchedAllergens);
        scan.setRiskLevel(riskLevel);
        scan.setSource(
                req.getBarcode() != null && !req.getBarcode().isBlank() ? "API" : "OCR"
        );
        scan.setImageUrl(imageUrl);
        scan.setCreatedAt(LocalDateTime.now());
        scan.setStatus("Evaluated");

        return productScanRepository.save(scan);
    }
}