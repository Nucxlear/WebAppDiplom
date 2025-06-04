package com.example.SalesAnalytics.controller;

import com.example.SalesAnalytics.model.Sale;

import com.example.SalesAnalytics.service.*;
import com.example.SalesAnalytics.repository.*;
import com.example.SalesAnalytics.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    // Получение продаж за последний месяц
    @GetMapping("/last-month")
    public List<ProductSalesSummary> getSalesLastMonth() {
        return saleService.getSalesLastMonth();
    }

    // Получение продаж за последний год
    @GetMapping("/last-year")
    public List<ProductSalesSummary> getSalesLastYear() {
        return saleService.getSalesLastYear();
    }

    // Получение продаж за произвольный период
    @GetMapping("/by-period")
    public List<ProductSalesSummary> getSalesByPeriod(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return saleService.getSalesByPeriod(startDate, endDate);
    }

    // Добавление новой продажи (пример)
    @PostMapping
    public Sale addSale(@RequestBody Sale sale) {
        // Здесь можно добавить логику валидации или расчет totalPrice
        sale.setTotalPrice(sale.getQuantity() * getProductPrice(sale.getProductId()));
        sale.setSaleDate(LocalDate.now());
        return saleRepository.save(sale); // Предполагается, что saleRepository доступен
    }

    // Вспомогательный метод для получения цены товара (нужен доступ к ProductRepository)
    private double getProductPrice(String productId) {
        // Логика для получения цены из ProductRepository
        return productRepository.findById(productId)
                .map(Product::getPrice)
                .orElse(0.0);
    }

    // Предполагаемые зависимости (добавьте в класс)
    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;
}