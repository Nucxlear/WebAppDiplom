package com.example.SalesAnalytics.controller;

import com.example.SalesAnalytics.model.*;
import com.example.SalesAnalytics.repository.*;
import com.example.SalesAnalytics.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductionRequestRepository productionRequestRepository;

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/sales")
    public String showHome(Model model, Authentication auth) {
        if (auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/sales/analytics";
        } else if (auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PRODUCTION_LEAD"))) {
            return "redirect:/sales/production";
        }
        return "redirect:/login";
    }

    @GetMapping("/sales/catalog")
    public String showCatalog(Model model, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/sales/production";
        }
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "catalog";
    }

    @PostMapping("/sales/add-product")
    public String addProduct(@RequestParam String name, @RequestParam String description, @RequestParam double price, @RequestParam int quantity, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/sales/production";
        }
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        productRepository.save(product);
        return "redirect:/sales/catalog";
    }

    @PostMapping("/sales/delete-product")
    public String deleteProduct(@RequestParam String id, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/sales/production";
        }
        productRepository.deleteById(id);
        return "redirect:/sales/catalog";
    }

    @PostMapping("/sales/sell-product")
    public String sellProduct(@RequestParam String id, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/sales/production";
        }
        Product product = productRepository.findById(id).orElse(null);
        if (product != null && product.getQuantity() > 0) {
            product.setQuantity(product.getQuantity() - 1);
            productRepository.save(product);
            Sale sale = new Sale();
            sale.setProductId(product.getId());
            sale.setQuantity(1);
            sale.setTotalPrice(product.getPrice());
            sale.setSaleDate(LocalDate.now());
            try {
                saleRepository.save(sale);
                System.out.println("Sale saved successfully: ID=" + sale.getId() + ", Product=" + product.getName() + ", TotalPrice=" + sale.getTotalPrice() + ", Date=" + sale.getSaleDate());
            } catch (Exception e) {
                System.out.println("Failed to save sale: " + e.getMessage());
            }
        } else {
            System.out.println("Sale failed: Product not found or quantity is 0 for ID " + id);
        }
        return "redirect:/sales/catalog";
    }

    @PostMapping("/sales/receive-product")
    public String receiveProduct(@RequestParam String id, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/sales/production";
        }
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            product.setQuantity(product.getQuantity() + 1);
            productRepository.save(product);
        }
        return "redirect:/sales/catalog";
    }

    @GetMapping("/sales/analytics")
    public String showAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/sales/production";
        }
        List<ProductSalesSummary> salesLastMonth = saleService.getSalesLastMonth();
        List<ProductSalesSummary> salesLastYear = saleService.getSalesLastYear();

        if (startDate != null && endDate != null) {
            model.addAttribute("salesByPeriod", saleService.getSalesByPeriod(startDate, endDate));
        } else {
            model.addAttribute("salesByPeriod", salesLastMonth);
        }

        model.addAttribute("salesLastMonth", salesLastMonth);
        model.addAttribute("salesLastYear", saleService.getSalesLastYear());

        if (startDate != null && endDate != null) {
            model.addAttribute("totalRevenue", saleService.calculateTotalRevenue(startDate, endDate));
            Map<String, Integer> productionPlan = saleService.calculateProductionPlan(startDate, endDate);
            model.addAttribute("productionPlanWithNames", convertProductionPlanToNames(productionPlan));
        } else {
            LocalDate defaultStart = LocalDate.now().minusMonths(1);
            LocalDate defaultEnd = LocalDate.now().plusDays(1);
            System.out.println("Default period for production plan: " + defaultStart + " to " + defaultEnd);
            model.addAttribute("totalRevenue", saleService.calculateTotalRevenue(defaultStart, defaultEnd));
            Map<String, Integer> productionPlan = saleService.calculateProductionPlan(defaultStart, defaultEnd);
            model.addAttribute("productionPlanWithNames", convertProductionPlanToNames(productionPlan));
        }
        return "analytics";
    }

    private Map<String, Integer> convertProductionPlanToNames(Map<String, Integer> productionPlan) {
        Map<String, Integer> planWithIds = new HashMap<>();
        for (Map.Entry<String, Integer> entry : productionPlan.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            productRepository.findById(productId).ifPresent(product ->
                    planWithIds.put(productId, quantity)
            );
        }
        return planWithIds;
    }

    @PostMapping("/sales/submit-request")
    public String submitRequest(@ModelAttribute ProductionRequest request, Model model, Authentication auth) {
        if (auth == null || !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            System.out.println("Access denied to /sales/submit-request. Authentication: " + auth + ", Authorities: " + (auth != null ? auth.getAuthorities() : "null"));
            return "redirect:/sales/analytics?error=access_denied";
        }
        try {
            ProductionRequest savedRequest = saleService.createProductionRequest(request.getProducts());
            System.out.println("Request submitted: ID=" + savedRequest.getId() + ", Products=" + savedRequest.getProducts());
            return "redirect:/sales/analytics?reset=true";
        } catch (Exception e) {
            System.out.println("Error submitting request: " + e.getMessage());
            model.addAttribute("error", "Ошибка при создании заявки: " + e.getMessage());
            return "redirect:/sales/analytics";
        }
    }

    @GetMapping("/sales/request/{id}")
    public String showRequest(@PathVariable String id, Model model, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/sales/production";
        }
        ProductionRequest request = productionRequestRepository.findById(id).orElse(null);
        if (request != null) {
            model.addAttribute("request", request);
        }
        return "request";
    }

    @GetMapping("/sales/production")
    public String showProductionDashboard(Model model, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PRODUCTION_LEAD"))) {
            return "redirect:/sales/analytics";
        }
        List<ProductionRequest> requests = productionRequestRepository.findAll();
        System.out.println("Production requests found: " + requests.size());
        for (ProductionRequest req : requests) {
            System.out.println("Request: ID=" + req.getId() + ", Status=" + req.getStatus() + ", Products=" + req.getProducts());
        }
        model.addAttribute("requests", requests);
        return "production";
    }

    @PostMapping("/sales/update-request-status")
    public String updateRequestStatus(@RequestParam String id, @RequestParam String status, Authentication auth) {
        if (auth != null && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PRODUCTION_LEAD"))) {
            return "redirect:/sales/analytics";
        }
        System.out.println("Updating request with id: " + id + ", new status: " + status);
        saleService.updateRequestAndQuantities(id, status);
        return "redirect:/sales/production";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/sales/generate-report")
    public String generateReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model, Authentication auth) {
        if (auth == null || !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            System.out.println("Access denied to /sales/generate-report. Authentication: " + auth + ", Authorities: " + (auth != null ? auth.getAuthorities() : "null"));
            return "redirect:/login?error=access_denied";
        }
        System.out.println("Generating report. StartDate: " + startDate + ", EndDate: " + endDate);
        model.addAttribute("salesLastMonth", saleService.getSalesLastMonth());
        model.addAttribute("salesLastYear", saleService.getSalesLastYear());
        model.addAttribute("totalRevenue", saleService.calculateTotalRevenue(LocalDate.now().minusMonths(1), LocalDate.now().plusDays(1)));
        model.addAttribute("productionPlanWithNames", convertProductionPlanToNames(saleService.calculateProductionPlan(LocalDate.now().minusMonths(1), LocalDate.now().plusDays(1))));
        return "report";
    }
}