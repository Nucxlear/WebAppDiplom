package com.example.SalesAnalytics.repository;

import com.example.SalesAnalytics.model.Sale;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends MongoRepository<Sale, String> {

    @Query("{ 'saleDate' : { $gte: ?0, $lte: ?1 } }")
    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);
}