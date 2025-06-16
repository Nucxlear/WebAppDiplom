package com.example.SalesAnalytics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "productionRequests")
public class ProductionRequest {
    @Id
    private String id;
    private Map<String, Integer> products;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Геттеры, сеттеры и конструкторы
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Map<String, Integer> getProducts() { return products; }
    public void setProducts(Map<String, Integer> products) { this.products = products; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
