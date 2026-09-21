package com.bank.controller;

import com.bank.entity.Account;
import com.bank.entity.AccountType;
import com.bank.entity.Transaction;
import com.bank.entity.TransactionType;
import com.bank.service.AccountService;
import com.bank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CUSTOMER')")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    // ✅ Create account
    @PostMapping
    public Account create(
            @RequestParam AccountType type,
            Authentication auth
    ) {
        return accountService.createAccount(type, auth.getName());
    }

    // ✅ Get single account
    @GetMapping("/{id}")
    public Account get(
            @PathVariable Long id,
            Authentication auth
    ) {
        return accountService.getAccount(id, auth.getName());
    }

    // ✅ Get logged‑in user's accounts
    @GetMapping
    public List<Account> getMyAccounts(Authentication auth) {
        return accountService.getMyAccounts(auth.getName());
    }

    // ✅ Deposit / Withdraw
    @PostMapping("/{accountId}/transact")
    public ResponseEntity<Map<String, Object>> transact(
            @PathVariable Long accountId,
            @RequestBody Map<String, Object> transactionData,
            Authentication auth
    ) {
        try {
            String frontendType = (String) transactionData.get("type");
            BigDecimal amount = new BigDecimal(transactionData.get("amount").toString());
            String description = (String) transactionData.getOrDefault(
                    "description", frontendType + " transaction");

            TransactionType backendType =
                    "DEPOSIT".equals(frontendType) ? TransactionType.CREDIT :
                    "WITHDRAW".equals(frontendType) ? TransactionType.DEBIT : null;

            if (backendType == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid transaction type"
                ));
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Amount must be positive"
                ));
            }

            Transaction tx = transactionService.doTransaction(
                    accountId, backendType, amount, description, auth.getName());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "transactionId", tx.getId(),
                "balanceAfter", tx.getBalanceAfter()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CUSTOMER')")
    public ResponseEntity<?> transferMoney(
            @RequestBody Map<String, Object> payload,
            Authentication auth
    ) {
        try {
            Long fromAccountId = Long.valueOf(payload.get("fromAccountId").toString());
            String toAccountNumber = payload.get("toAccountNumber").toString();
            String toCustomerId = payload.get("toCustomerId").toString();
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            String description = payload.getOrDefault(
                    "description", "Account transfer"
            ).toString();

            accountService.transferByAccountNumber(
                    fromAccountId,
                    toAccountNumber,
                    toCustomerId,
                    amount,
                    description,
                    auth.getName()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Transfer successful"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
    

    // ✅ Transaction history (ONLY THIS VERSION SHOULD EXIST)
    @GetMapping("/{accountId}/transactions")
    public List<Transaction> getTransactions(
            @PathVariable Long accountId,
            Authentication auth
    ) {
        return transactionService.getTransactions(accountId, auth.getName());
    }
}
