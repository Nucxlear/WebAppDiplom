package com.example.SalesAnalytics.controller;

import com.example.SalesAnalytics.model.Product;
import com.example.SalesAnalytics.repository.ProductRepository;
import com.example.SalesAnalytics.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/sales")
    public String showHome(Model model) {
        return "index";
    }

    @GetMapping("/sales/catalog")
    public String showCatalog(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "catalog";
    }

    @PostMapping("/sales/add-product")
    public String addProduct(@RequestParam String name, @RequestParam String description, @RequestParam double price) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        productRepository.save(product);
        return "redirect:/sales/catalog";
    }

    @PostMapping("/sales/delete-product")
    public String deleteProduct(@RequestParam String id) {
        productRepository.deleteById(id);
        return "redirect:/sales/catalog";
    }

    @GetMapping("/sales/analytics")
    public String showAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        // Продажи за последний месяц и год
        List<ProductSalesSummary> salesLastMonth = saleService.getSalesLastMonth();
        List<ProductSalesSummary> salesLastYear = saleService.getSalesLastYear();

        // Если указаны даты, используем их для фильтра
        if (startDate != null && endDate != null) {
            model.addAttribute("salesByPeriod", saleService.getSalesByPeriod(startDate, endDate));
        } else {
            model.addAttribute("salesByPeriod", salesLastMonth); // По умолчанию последний месяц
        }

        model.addAttribute("salesLastMonth", salesLastMonth);
        model.addAttribute("salesLastYear", salesLastYear);

        // Существующие данные аналитики
        if (startDate != null && endDate != null) {
            model.addAttribute("totalRevenue", saleService.calculateTotalRevenue(startDate, endDate));
            model.addAttribute("monthlySales", saleService.getMonthlySalesSummary(startDate, endDate));
            model.addAttribute("productionPlan", saleService.calculateProductionPlan(startDate, endDate));
        } else {
            LocalDate defaultStart = LocalDate.now().minusMonths(1);
            LocalDate defaultEnd = LocalDate.now();
            model.addAttribute("totalRevenue", saleService.calculateTotalRevenue(defaultStart, defaultEnd));
            model.addAttribute("monthlySales", saleService.getMonthlySalesSummary(defaultStart, defaultEnd));
            model.addAttribute("productionPlan", saleService.calculateProductionPlan(defaultStart, defaultEnd));
        }
        return "analytics";
    }
}