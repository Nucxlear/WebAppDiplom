package com.example.SalesAnalytics.repository;

import com.example.SalesAnalytics.model.Sale;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends MongoRepository<Sale, String> {
    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);
}