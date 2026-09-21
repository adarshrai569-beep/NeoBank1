package com.bank.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bank.dto.BillDTO;
import com.bank.entity.Bill;
import com.bank.service.BillService;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService service;

    @GetMapping
    public List<Bill> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BillDTO dto) {

        Bill bill = Bill.builder()
                .billerName(dto.getBillerName())
                .amount(dto.getAmount())
                .dueDate(dto.getDueDate())
                .build();

        return ResponseEntity.status(201).body(service.create(bill));
    }

    // ✅ NEW payment API
    @PatchMapping("/{id}/pay")
    public ResponseEntity<?> payBill(
            @PathVariable Long id,
            @RequestParam String method) {

        return ResponseEntity.ok(service.payBill(id, method));
    }
}