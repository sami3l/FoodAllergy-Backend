package com.sour.Backend_foodAllergy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {

    private List<String> allergens;
    private String riskLevel;
    private String productName;
    private String imageUrl;
    private String source;
    private LocalDateTime timestamp;

    public ScanResponse(String riskLevel, List<String> matchedAllergens) {
        this.riskLevel = riskLevel;
        this.allergens = matchedAllergens;
    }
}
