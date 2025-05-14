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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AllergyController {

    private final UserRepository userRepository;
    private final ProductScanRepository scanRepository;
    private final AllergyService allergyService;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final ProductScanService productScanService;

    public AllergyController(UserRepository userRepository,
                             ProductScanRepository scanRepository,
                             AllergyService allergyService,
                             OpenFoodFactsClient openFoodFactsClient,
                             ProductScanService productScanService) {
        this.userRepository = userRepository;
        this.scanRepository = scanRepository;
        this.allergyService = allergyService;
        this.openFoodFactsClient = openFoodFactsClient;
        this.productScanService = productScanService;
    }

    @GetMapping("/hello")
    public String sayhi() {
        return "Hello from FoodAllergyAI 👋";
    }

    @PostMapping(value = "/evaluate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> evaluate(@RequestBody ScanRequest request) {
        try {
            // Using the new ProductScanService to handle all the scan logic
            ScanResponse response = productScanService.scanProduct(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<?> getUserScanHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // Using the new ProductScanService to get user scan history
            Page<ProductScan> response = productScanService.getScansByUser(userId, page, size);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/scan/{scanId}")
    public ResponseEntity<?> getScanDetails(@PathVariable String scanId) {
        Optional<ProductScan> scanOpt = scanRepository.findById(scanId);
        if (scanOpt.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(scanOpt.get());
    }

    @DeleteMapping("/scan/{scanId}")
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
    public ResponseEntity<?> deleteAllUserScans(@PathVariable String userId) {
        Optional<User> userOpt = userRepository.findById(String.valueOf(Long.valueOf(userId)));
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        scanRepository.deleteByUserId(userId);
        return ResponseEntity.ok().body("All scan history deleted successfully");
    }

    @GetMapping("/user/{userId}/allergies")
    public ResponseEntity<?> getUserAllergies(@PathVariable String userId) {
        Optional<User> userOpt = userRepository.findById(String.valueOf(Long.valueOf(userId)));
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        List<String> allergies = parseAllergiesList(user.getAllergies().toString());

        return ResponseEntity.ok(allergies);
    }

    @PutMapping("/user/{userId}/allergies")
    public ResponseEntity<?> updateUserAllergies(
            @PathVariable String userId,
            @RequestBody AllergyUpdateRequest request) {

        Optional<User> userOpt = userRepository.findById(String.valueOf(Long.valueOf(userId)));
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

        ProductInfoResponse productInfo = openFoodFactsClient.getProductInfo(barcode);
        if (productInfo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(productInfo);
    }

    @GetMapping("/allergens/common")
    public ResponseEntity<?> getCommonAllergens() {
        List<String> commonAllergens = allergyService.getCommonAllergens();
        return ResponseEntity.ok(commonAllergens);
    }

    private List<String> parseAllergiesList(String allergiesString) {
        if (allergiesString == null || allergiesString.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(allergiesString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}