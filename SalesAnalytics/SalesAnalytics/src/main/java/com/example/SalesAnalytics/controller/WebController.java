package com.example.SalesAnalytics.controller;

import com.example.SalesAnalytics.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class WebController {

    @Autowired
    private SaleRepository saleRepository;

    @GetMapping("/sales")
    public String showSales(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            Model model) {
        if (startDate != null && endDate != null) {
            model.addAttribute("sales", saleRepository.findBySaleDateBetween(startDate, endDate));
        } else {
            model.addAttribute("sales", saleRepository.findAll());
        }
        return "index";
    }
}
