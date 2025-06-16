package com.example.SalesAnalytics.service;

import com.example.SalesAnalytics.model.*;
import com.example.SalesAnalytics.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private ProductionRequestRepository productionRequestRepository;

    // Храним последнюю дату отправки заявки
    private LocalDateTime lastRequestSubmission = null;

    public List<ProductSalesSummary> getSalesLastMonth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        return getSalesSummary(startDate, endDate);
    }

    public List<ProductSalesSummary> getSalesLastYear() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);
        return getSalesSummary(startDate, endDate);
    }

    public List<ProductSalesSummary> getSalesByPeriod(LocalDate startDate, LocalDate endDate) {
        return getSalesSummary(startDate, endDate);
    }

    private List<ProductSalesSummary> getSalesSummary(LocalDate startDate, LocalDate endDate) {
        LocalDate adjustedEndDate = endDate.plusDays(1);
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, adjustedEndDate);
        System.out.println("Sales found for period " + startDate + " to " + endDate + ": " + sales.size());
        for (Sale sale : sales) {
            System.out.println("Sale: ID=" + sale.getId() + ", ProductId=" + sale.getProductId() + ", SaleDate=" + sale.getSaleDate() + ", TotalPrice=" + sale.getTotalPrice());
        }
        Map<String, Product> productMap = productRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        Map<String, ProductSalesSummary> salesSummaryMap = new HashMap<>();
        for (Sale sale : sales) {
            String productId = sale.getProductId();
            Product product = productMap.get(productId);
            if (product != null) {
                salesSummaryMap.compute(productId, (key, oldValue) -> {
                    if (oldValue == null) {
                        return new ProductSalesSummary(product.getName(), sale.getQuantity(), sale.getTotalPrice());
                    } else {
                        int newQuantity = oldValue.getTotalQuantity() + sale.getQuantity();
                        double newRevenue = oldValue.getTotalRevenue() + sale.getTotalPrice();
                        return new ProductSalesSummary(product.getName(), newQuantity, newRevenue);
                    }
                });
            } else {
                System.out.println("Product not found for ProductId: " + productId);
            }
        }
        return salesSummaryMap.values().stream().collect(Collectors.toList());
    }

    public double calculateTotalRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDate adjustedEndDate = endDate.plusDays(1);
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, adjustedEndDate);
        System.out.println("Sales for revenue calculation: " + sales.size() + " from " + startDate + " to " + endDate);
        for (Sale sale : sales) {
            System.out.println("Revenue Sale: ID=" + sale.getId() + ", TotalPrice=" + sale.getTotalPrice() + ", Date=" + sale.getSaleDate());
        }
        return sales.stream()
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
        LocalDate adjustedEndDate = (endDate != null) ? endDate.plusDays(1) : LocalDate.now().plusDays(1);
        startDate = (startDate != null) ? startDate : LocalDate.now().minusMonths(1);

        LocalDate effectiveStartDate = (lastRequestSubmission != null) ? LocalDate.from(lastRequestSubmission) : startDate;
        List<Sale> sales = saleRepository.findBySaleDateBetween(effectiveStartDate, adjustedEndDate);
        Map<String, Integer> productionPlan = new HashMap<>();
        for (Sale sale : sales) {
            productionPlan.merge(sale.getProductId(), sale.getQuantity(), Integer::sum);
        }

        System.out.println("Production plan calculated for " + effectiveStartDate + " to " + endDate + ": " + productionPlan);
        return productionPlan;
    }

    public ProductionRequest createProductionRequest(Map<String, Integer> productionPlan) {
        ProductionRequest request = new ProductionRequest();
        request.setProducts(productionPlan);
        request.setStatus("В обработке");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        ProductionRequest savedRequest = productionRequestRepository.save(request);

        lastRequestSubmission = LocalDateTime.now();
        return savedRequest;
    }

    @Transactional
    public void updateRequestAndQuantities(String requestId, String newStatus) {
        ProductionRequest request = productionRequestRepository.findById(requestId).orElse(null);
        System.out.println("Processing request " + requestId + ", new status: " + newStatus);
        if (request != null) {
            request.setStatus(newStatus);
            request.setUpdatedAt(LocalDateTime.now());
            productionRequestRepository.save(request);
            System.out.println("Request saved with status: " + request.getStatus());

            if ("Выполнено".equals(newStatus)) {
                Map<String, Integer> products = request.getProducts();
                System.out.println("Products in request: " + products);
                for (Map.Entry<String, Integer> entry : products.entrySet()) {
                    String productId = entry.getKey();
                    int quantity = entry.getValue();
                    Product product = productRepository.findById(productId).orElse(null);
                    if (product != null) {
                        int newQuantity = product.getQuantity() + quantity;
                        product.setQuantity(newQuantity);
                        productRepository.save(product);
                        System.out.println("Updated quantity for productId " + productId + " from " + (newQuantity - quantity) + " to " + newQuantity);
                    } else {
                        System.out.println("Product not found for productId: " + productId + ". Check if ID matches a product in the catalog.");
                    }
                }
            }
        } else {
            System.out.println("Request not found for id: " + requestId);
        }
    }
}