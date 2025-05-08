package com.sour.Backend_foodAllergy.controller;

import com.sour.Backend_foodAllergy.dto.ProductInfoResponse;
import com.sour.Backend_foodAllergy.model.ProductScan;
import com.sour.Backend_foodAllergy.model.User;
import com.sour.Backend_foodAllergy.repository.ProductScanRepository;
import com.sour.Backend_foodAllergy.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ProductScanRepository scanRepository;

    public UserController(UserRepository userRepository, ProductScanRepository scanRepository) {
        this.userRepository = userRepository;
        this.scanRepository = scanRepository;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userRepository.save(user));
    }

    // Using String for the user ID (no ObjectId)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(Long.valueOf(id));  // No need for ObjectId conversion
        return userOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Using String for the userId in the scan method (no ObjectId)
    @GetMapping("/scans/user/{userId}")
    public ResponseEntity<?> getScansByUser(@PathVariable String userId) {
        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));  // Directly use String for userId
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Use the String version of the method
        List<ProductScan> scans = scanRepository.findByUserId(Long.valueOf(userId));

        List<ProductInfoResponse> response = scans.stream().map(scan ->
                new ProductInfoResponse(
                        scan.getProductName(),
                        scan.getBarcode(),
                        scan.getProductText(),      // Map this to 'ingredients'
                        scan.getImageUrl(),         // Make sure this is not null!
                        null,                       // Brand (if needed)
                        null,                       // Categories (optional)
                        null,                       // Nutriscore
                        List.of(),                  // Allergens (optional)
                        null                        // Serving size
                )
        ).toList();

        return ResponseEntity.ok(response);
    }
}
