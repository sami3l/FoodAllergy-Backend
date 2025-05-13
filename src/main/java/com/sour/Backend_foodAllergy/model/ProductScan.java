package com.sour.Backend_foodAllergy.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_scan")
public class ProductScan {

    @Id
    private String id;

    private String productText;
    private String imageUrl;
    private List<String> detectedAllergens;
    private String riskLevel;
    private String source; // "OCR" || "API"

    private String productName;
    private String barcode;

  // Utiliser ObjectId au lieu de Long ??

    private String userId;

    @CreatedDate
    private LocalDateTime createdAt;

    private String status;
}
