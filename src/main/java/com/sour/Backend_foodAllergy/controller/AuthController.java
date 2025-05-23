package com.sour.Backend_foodAllergy.controller;

import com.sour.Backend_foodAllergy.dto.AuthRequest;
import com.sour.Backend_foodAllergy.dto.AuthResponse;
import com.sour.Backend_foodAllergy.model.User;
import com.sour.Backend_foodAllergy.repository.UserRepository;
import com.sour.Backend_foodAllergy.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtil;
    private final UserRepository userRepository; // Inject your repository

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest request) {
        log.info("LOGIN attempt for username: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(401).body("Incorrect username or password");
        }

        // Fetch the user entity from the repository
        User userEntity = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (userEntity == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        final String jwt = jwtUtil.generateToken(userEntity.getUsername());

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                userEntity.getUsername(),
                userEntity.getId() // This is a String, since you extend MongoRepository<User, String>
        ));
    }
}
