package com.bank.controller;

import com.bank.entity.Transaction;
import com.bank.entity.TransactionType;
import com.bank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")   // ✅ CHANGED HERE
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CUSTOMER')")
public class TransactionController {

    private final TransactionService transactionService;

    // ✅ Execute transaction
    @PostMapping
    public Transaction transact(
            @RequestParam Long accountId,
            @RequestParam TransactionType type,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            Authentication auth
    ) {
        return transactionService.doTransaction(
                accountId, type, amount, description, auth.getName()
        );
    }

    // ✅ Optional: history via transactions controller
    @GetMapping
    public List<Transaction> history(
            @RequestParam Long accountId,
            Authentication auth
    ) {
        return transactionService.getTransactions(accountId, auth.getName());
    }
}