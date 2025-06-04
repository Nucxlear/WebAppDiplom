package com.example.SalesAnalytics.service;

import com.example.SalesAnalytics.model.*;
import com.example.SalesAnalytics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    // Продажи за последний месяц
    public List<ProductSalesSummary> getSalesLastMonth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        return getSalesSummary(startDate, endDate);
    }

    // Продажи за последний год
    public List<ProductSalesSummary> getSalesLastYear() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);
        return getSalesSummary(startDate, endDate);
    }

    // Общий метод для получения статистики продаж за произвольный период
    public List<ProductSalesSummary> getSalesByPeriod(LocalDate startDate, LocalDate endDate) {
        return getSalesSummary(startDate, endDate);
    }

    // Общий метод для получения статистики продаж
    private List<ProductSalesSummary> getSalesSummary(LocalDate startDate, LocalDate endDate) {
        // Получаем продажи за период
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);

        // Получаем все товары
        Map<String, Product> productMap = productRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        // Группируем продажи по товару
        Map<String, ProductSalesSummary> salesSummaryMap = new HashMap<>();
        for (Sale sale : sales) {
            String productId = sale.getProductId();
            Product product = productMap.get(productId);
            if (product == null) continue; // Пропускаем, если товар не найден

            String productName = product.getName();
            salesSummaryMap.compute(productId, (key, oldValue) -> {
                if (oldValue == null) {
                    return new ProductSalesSummary(productName, sale.getQuantity(), sale.getTotalPrice());
                } else {
                    int newQuantity = oldValue.getTotalQuantity() + sale.getQuantity();
                    double newRevenue = oldValue.getTotalRevenue() + sale.getTotalPrice();
                    return new ProductSalesSummary(productName, newQuantity, newRevenue);
                }
            });
        }

        return salesSummaryMap.values().stream().collect(Collectors.toList());
    }

    // Существующие методы (оставляем без изменений)
    public double calculateTotalRevenue(LocalDate startDate, LocalDate endDate) {
        return saleRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .mapToDouble(Sale::getTotalPrice)
                .sum();
    }

    public Map<String, Double> getMonthlySalesSummary(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> monthlySales = new HashMap<>();
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);
        for (Sale sale : sales) {
            String monthKey = sale.getSaleDate().getMonth().toString() + " " + sale.getSaleDate().getYear();
            monthlySales.merge(monthKey, sale.getTotalPrice(), Double::sum);
        }
        return monthlySales;
    }

    public Map<String, Integer> calculateProductionPlan(LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> productionPlan = new HashMap<>();
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);
        Map<String, Product> productMap = productRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (Sale sale : sales) {
            Product product = productMap.get(sale.getProductId());
            if (product != null) {
                productionPlan.merge(product.getName(), sale.getQuantity(), Integer::sum);
            }
        }
        return productionPlan;
    }
}
