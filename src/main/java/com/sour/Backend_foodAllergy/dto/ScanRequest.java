package com.sour.Backend_foodAllergy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequest {
    // Changed from Long to String to match MongoDB ID format
    private String productText; // for OCR text
    private String barcode;     // for OpenFoodFacts
    private String productName; // Added for storing product name

}
