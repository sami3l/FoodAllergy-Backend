package com.sour.Backend_foodAllergy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username; // add this
    private String userId;   // add this (or id)
}