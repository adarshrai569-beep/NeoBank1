package com.bank.controller;

import com.bank.entity.LoanAccount;
import com.bank.entity.LoanRepayment;
import com.bank.repository.*;
import com.bank.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class UserAnalyticsController {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final SecurityUtil securityUtil;

    // GET /api/analytics/spending/{userId}?months=6
    @GetMapping("/spending/{userId}")
    public ResponseEntity<?> getSpendingAnalytics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "6") int months) {

        Long jwtUserId = securityUtil.getUserId();
        if (!jwtUserId.equals(userId)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        LocalDateTime from = LocalDateTime.now().minusMonths(months);
        List<Object[]> categorySpending = transactionRepository.getCategorySpending(userId, from);
        List<Object[]> monthlyCategorySpending = transactionRepository.getMonthlyCategorySpending(userId, from);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categorySpending", categorySpending);
        result.put("monthlyCategorySpending", monthlyCategorySpending);
        result.put("months", months);

        return ResponseEntity.ok(result);
    }

    // GET /api/analytics/wealth/{userId}
    @GetMapping("/wealth/{userId}")
    public ResponseEntity<?> getWealthAnalytics(@PathVariable Long userId) {

        Long jwtUserId = securityUtil.getUserId();
        if (!jwtUserId.equals(userId)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        // Total account balances
        BigDecimal totalBalance = accountRepository.findByUserEmail(
                securityUtil.getEmail()).stream()
                .filter(a -> a.getIsActive())
                .map(a -> a.getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Outstanding loan principal
        List<LoanAccount> loanAccounts = loanAccountRepository.findByUserId(userId);
        BigDecimal outstandingPrincipal = BigDecimal.ZERO;
        List<Map<String, Object>> loanPayoffForecast = new ArrayList<>();

        for (LoanAccount loan : loanAccounts) {
            List<LoanRepayment> pendingRepayments = loanRepaymentRepository
                    .findByLoanAccountIdAndPaymentStatus(loan.getId(), "PENDING");

            BigDecimal loanOutstanding = pendingRepayments.stream()
                    .map(LoanRepayment::getPrincipalComponent)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            outstandingPrincipal = outstandingPrincipal.add(loanOutstanding);

            Map<String, Object> forecast = new LinkedHashMap<>();
            forecast.put("loanAccountId", loan.getId());
            forecast.put("principalAmount", loan.getPrincipalAmount());
            forecast.put("outstandingPrincipal", loanOutstanding);
            forecast.put("monthsRemaining", pendingRepayments.size());
            forecast.put("emiAmount", loan.getEmiAmount());
            loanPayoffForecast.add(forecast);
        }

        BigDecimal netWorth = totalBalance.subtract(outstandingPrincipal);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBalance", totalBalance);
        result.put("outstandingPrincipal", outstandingPrincipal);
        result.put("netWorth", netWorth);
        result.put("loanPayoffForecast", loanPayoffForecast);

        return ResponseEntity.ok(result);
    }
}
