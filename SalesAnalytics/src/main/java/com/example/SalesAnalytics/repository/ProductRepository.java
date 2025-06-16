package com.example.SalesAnalytics.repository;

import com.example.SalesAnalytics.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByName(String name);
}
