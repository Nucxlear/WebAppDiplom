package com.example.SalesAnalytics.repository;

import com.example.SalesAnalytics.model.ProductionRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductionRequestRepository extends MongoRepository<ProductionRequest, String> {
}
