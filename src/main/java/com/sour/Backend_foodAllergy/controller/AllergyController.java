package com.sour.Backend_foodAllergy.controller;

import com.sour.Backend_foodAllergy.dto.AllergyUpdateRequest;
import com.sour.Backend_foodAllergy.dto.ProductInfoResponse;
import com.sour.Backend_foodAllergy.dto.ScanRequest;
import com.sour.Backend_foodAllergy.dto.ScanResponse;
import com.sour.Backend_foodAllergy.model.ProductScan;
import com.sour.Backend_foodAllergy.model.User;
import com.sour.Backend_foodAllergy.repository.ProductScanRepository;
import com.sour.Backend_foodAllergy.repository.UserRepository;
import com.sour.Backend_foodAllergy.service.AllergyService;
import com.sour.Backend_foodAllergy.service.ProductScanService;
import com.sour.Backend_foodAllergy.utils.OpenFoodFactsClient;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AllergyController {

    private final UserRepository userRepository;
    private final ProductScanRepository scanRepository;
    private final AllergyService allergyService;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final ProductScanService productScanService;

    @GetMapping("/hello")
    public String sayhi() {
        return "Hello from FoodAllergyAI 👋";
    }




    @PostMapping(value = "/evaluate", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> evaluate(
            @RequestBody ScanRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        try {
            // on récupère l'identifiant (ou username) depuis le JWT
            String username = currentUser.getUsername();
            ScanResponse response = productScanService.scanProduct(request, username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 404 si vraiment introuvable
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserScanHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!isCurrentUser(userId) && !hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        try {
            Page<ProductScan> response = productScanService.getScansByUser(userId, page, size);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/scan/{scanId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getScanDetails(@PathVariable String scanId) {
        Optional<ProductScan> scanOpt = scanRepository.findById(scanId);
        return scanOpt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/scan/{scanId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteScan(@PathVariable String scanId, @RequestParam String userId) {
        Optional<ProductScan> scanOpt = scanRepository.findById(scanId);
        if (scanOpt.isEmpty()) return ResponseEntity.notFound().build();

        ProductScan scan = scanOpt.get();
        if (!scan.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to delete this scan");
        }

        scanRepository.delete(scan);
        return ResponseEntity.ok().body("Scan deleted successfully");
    }

    @DeleteMapping("/user/{userId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAllUserScans(@PathVariable String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        scanRepository.deleteByUserId(userId);
        return ResponseEntity.ok().body("All scan history deleted successfully");
    }

    @GetMapping("/user/{userId}/allergies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserAllergies(@PathVariable String userId) {
        if (!isCurrentUser(userId) && !hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        List<String> allergies = parseAllergiesList(user.getAllergies().toString());
        return ResponseEntity.ok(allergies);
    }

    @PutMapping("/user/{userId}/allergies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserAllergies(
            @PathVariable String userId,
            @RequestBody AllergyUpdateRequest request) {

        if (!isCurrentUser(userId) && !hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        user.setAllergies(Collections.singletonList(String.join(",", request.getAllergies())));
        userRepository.save(user);

        return ResponseEntity.ok().body("Allergies updated successfully");
    }

    @GetMapping("/product/info")
    public ResponseEntity<?> getProductInfo(@RequestParam String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            return ResponseEntity.badRequest().body("Barcode is required");
        }

        // Appel à OpenFoodFacts
        ProductInfoResponse productInfo = openFoodFactsClient.getProductInfo(barcode);
        if (productInfo == null) {
            return ResponseEntity.notFound().build();
        }

        // Sauvegarde temporaire d’un ProductScan pour générer un id
        ProductScan scan = new ProductScan();
        scan.setBarcode(barcode);
        scan.setProductName(productInfo.getProductName());
        scan.setProductText(productInfo.getIngredients());
        scan.setImageUrl(productInfo.getImageUrl());
        scan.setSource("API");
        scan.setStatus("Pending");
        scan.setCreatedAt(java.time.LocalDateTime.now());


        // Injecter l'id dans la réponse
        productInfo.setId(scan.getId());

        return ResponseEntity.ok(productInfo);
    }


    @GetMapping("/allergens/common")
    public ResponseEntity<?> getCommonAllergens() {
        List<String> commonAllergens = allergyService.getCommonAllergens();
        return ResponseEntity.ok(commonAllergens);
    }

    // 🔐 Méthodes utilitaires pour rôle et ID utilisateur
    private boolean isCurrentUser(String userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getName().equals(userId);
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private List<String> parseAllergiesList(String allergiesString) {
        if (allergiesString == null || allergiesString.isEmpty()) return new ArrayList<>();
        return Arrays.stream(allergiesString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
