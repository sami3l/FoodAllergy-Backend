package com.sour.Backend_foodAllergy.repository;

import com.sour.Backend_foodAllergy.model.ProductScan;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductScanRepository extends MongoRepository<ProductScan, String> {


    void deleteByUserId(String userId);  // String version

    Page<ProductScan> findByUserId(String userId, Pageable pageable);  // String version with pagination

    List<ProductScan> findByUserId(String userId);  // ObjectId version
}
