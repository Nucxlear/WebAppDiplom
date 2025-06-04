package com.example.SalesAnalytics.repository;

import com.example.SalesAnalytics.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
