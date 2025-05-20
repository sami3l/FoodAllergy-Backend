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

    public class ScanResponse {
        private List<String> detectedAllergens; // déjà présent
        private String riskLevel;
        private String productName;
        private String imageUrl;
        private String source;
        private LocalDateTime createdAt;
        private List<String> ingredients;   // AJOUTE CETTE LIGNE

        // constructeur à mettre à jour :
        public ScanResponse(List<String> detectedAllergens,
                            String riskLevel,
                            String productName,
                            String imageUrl,
                            String source,
                            LocalDateTime createdAt,
                            List<String> ingredients) {      // AJOUTE CET ARGUMENT
            this.detectedAllergens = detectedAllergens;
            this.riskLevel = riskLevel;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.source = source;
            this.createdAt = createdAt;
            this.ingredients = ingredients;   // AJOUT
        }
        // ... getters/setters pour ingredients
    }