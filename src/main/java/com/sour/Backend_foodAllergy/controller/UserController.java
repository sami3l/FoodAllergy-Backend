package com.sour.Backend_foodAllergy.controller;

import com.sour.Backend_foodAllergy.dto.ProductInfoResponse;
import com.sour.Backend_foodAllergy.model.ProductScan;
import com.sour.Backend_foodAllergy.model.User;
import com.sour.Backend_foodAllergy.repository.ProductScanRepository;
import com.sour.Backend_foodAllergy.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ProductScanRepository scanRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, ProductScanRepository scanRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.scanRepository = scanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
    }

    // Existing endpoint - modified to remove password from response
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Clear password before returning
        savedUser.setPassword(null);
        return ResponseEntity.ok(savedUser);
    }

    // Using String for the user ID (no ObjectId)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(String.valueOf(Long.valueOf(id)));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Don't expose password in API responses
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Don't expose passwords in API responses
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    // Using String for the userId in the scan method (no ObjectId)
    @GetMapping("/scans/user/{userId}")
    public ResponseEntity<?> getScansByUser(@PathVariable String userId) {
        Optional<User> userOpt = userRepository.findById(String.valueOf(Long.valueOf(userId)));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Use the String version of the method
        List<ProductScan> scans = scanRepository.findByUserId(userId);

        List<ProductInfoResponse> response = scans.stream().map(scan ->
                new ProductInfoResponse(
                        scan.getProductName(),
                        scan.getBarcode(),
                        scan.getProductText(),
                        scan.getImageUrl(),
                        null,
                        null,
                        null,
                        List.of(),
                        null
                )
        ).toList();

        return ResponseEntity.ok(response);
    }

    // Add a new endpoint to find user by username (useful for testing)
    @GetMapping("/by-username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Don't expose password in API responses
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}