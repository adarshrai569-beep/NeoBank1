package com.bank.controller;

import com.bank.dto.LoanProductDTO;
import com.bank.service.LoanProductService;
import com.bank.util.AuditLogger;
import com.bank.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans/products")
@RequiredArgsConstructor
public class LoanProductController {

    private final LoanProductService loanProductService;
    private final SecurityUtil securityUtil;
    private final AuditLogger auditLogger;

    // ✅ CREATE LOAN PRODUCT (ADMIN ONLY)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createLoanProduct(@RequestBody LoanProductDTO dto) {
        LoanProductDTO createdProduct = loanProductService.create(dto);
        if (createdProduct != null && createdProduct.getId() != null) {
            auditLogger.logAdminAction(securityUtil.getUserId(), "LOAN_PRODUCT_CREATE", "LOAN_PRODUCT", createdProduct.getId());
        }
        return ResponseEntity.status(201).body(createdProduct);
    }

    // ✅ GET ALL LOAN PRODUCTS (ALL AUTHENTICATED USERS)
    @GetMapping
    public ResponseEntity<?> getAllLoanProducts() {
        List<LoanProductDTO> products = loanProductService.getAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLoanProduct(@PathVariable Long id) {
        return ResponseEntity.ok(loanProductService.getById(id));
    }
}