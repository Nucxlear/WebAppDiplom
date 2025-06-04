package com.example.SalesAnalytics.service;

public class ProductSalesSummary {

    private String productName;
    private int totalQuantity;
    private double totalRevenue;

    public ProductSalesSummary(String productName, int totalQuantity, double totalRevenue) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }

    // Геттеры
    public String getProductName() {
        return productName;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}
