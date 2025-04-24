package com.sour.Backend_foodAllergy.repository;

import com.sour.Backend_foodAllergy.model.ProductScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductScanRepository extends MongoRepository<ProductScan, String> {
    List<ProductScan> findByUserId(Long userId);  // For simple list without pagination

    void deleteByUserId(String userId);  // Delete method (ensure userId type matches)

    // Corrected method with Page return type (recommended for pagination)
    Page<ProductScan> findByUserId(String userId, Pageable pageable);
}