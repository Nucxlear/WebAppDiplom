package com.example.SalesAnalytics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "sales")
public class Sale {
    @Id
    private String id;

    private String productId;
    private int quantity;
    private double totalPrice;
    private LocalDate saleDate;

    public Sale() {}

    public Sale(String productId, int quantity, double totalPrice, LocalDate saleDate) {
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.saleDate = saleDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
}