package com.bank.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bank.entity.Budget;
import com.bank.service.BudgetService;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService service;

    // ✅ CREATE BUDGET
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Budget b) {
        return ResponseEntity.status(201).body(service.create(b));
    }

    // ✅ GET BY MONTH (SAFE VALIDATION)
    
    @GetMapping("/{month}")
    public ResponseEntity<?> get(@PathVariable String month) {

        try {
            // ✅ parse YearMonth instead of LocalDate
            java.time.YearMonth ym = java.time.YearMonth.parse(month);

            // ✅ convert to first day of month
            LocalDate m = ym.atDay(1);

            return ResponseEntity.ok(service.getBudgetSummary(m));

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid month format. Use YYYY-MM");
        }
    }

    // ✅ DELETE BUDGET BY ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
}