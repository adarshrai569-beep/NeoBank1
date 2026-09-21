package com.bank.controller;

import com.bank.entity.SystemAuditLog;
import com.bank.repository.SystemAuditLogRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.LoanApplicationRepository;
import com.bank.repository.LoanAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final TransactionRepository transactionRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final SystemAuditLogRepository auditLogRepository;

    // GET /api/admin/analytics/transactions?timeframe=7d|30d|YTD
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactionAnalytics(@RequestParam(defaultValue = "30d") String timeframe) {
        LocalDateTime from = resolveTimeframe(timeframe);
        if (from == null) {
            return ResponseEntity.badRequest().body("Invalid timeframe. Use 7d, 30d, or YTD");
        }

        List<Object[]> dailyVolumes = transactionRepository.getDailyVolumes(from);
        Double avgTicketSize = transactionRepository.getAverageTicketSize(from);
        Double totalInflow = transactionRepository.getTotalByType(com.bank.entity.TransactionType.CREDIT, from);
        Double totalOutflow = transactionRepository.getTotalByType(com.bank.entity.TransactionType.DEBIT, from);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timeframe", timeframe);
        result.put("dailyVolumes", dailyVolumes);
        result.put("averageTicketSize", avgTicketSize != null ? avgTicketSize : 0.0);
        result.put("totalInflow", totalInflow != null ? totalInflow : 0.0);
        result.put("totalOutflow", totalOutflow != null ? totalOutflow : 0.0);

        return ResponseEntity.ok(result);
    }

    // GET /api/admin/analytics/loans?timeframe=7d|30d|YTD
    @GetMapping("/loans")
    public ResponseEntity<?> getLoanAnalytics(@RequestParam(defaultValue = "30d") String timeframe) {
        LocalDateTime from = resolveTimeframe(timeframe);
        if (from == null) {
            return ResponseEntity.badRequest().body("Invalid timeframe. Use 7d, 30d, or YTD");
        }

        long pending = loanApplicationRepository.countByStatus("PENDING");
        long approved = loanApplicationRepository.countByStatus("APPROVED");
        long rejected = loanApplicationRepository.countByStatus("REJECTED");

        long totalLoans = loanAccountRepository.count();
        long overdueLoans = loanAccountRepository.countOverdueLoans();
        double npaRatio = totalLoans > 0 ? (double) overdueLoans / totalLoans * 100 : 0.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timeframe", timeframe);
        result.put("loanDistribution", Map.of("PENDING", pending, "APPROVED", approved, "REJECTED", rejected));
        result.put("npaCount", overdueLoans);
        result.put("npaRatio", Math.round(npaRatio * 100.0) / 100.0);
        result.put("totalLoanAccounts", totalLoans);

        return ResponseEntity.ok(result);
    }

    // GET /api/admin/system-logs?page=0&size=20&status=400
    @GetMapping("/system-logs")
    public ResponseEntity<?> getSystemLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTimestamp"));
        Page<SystemAuditLog> logs;

        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();

        if (status != null) {
            logs = auditLogRepository.findByResponseStatusBetweenAndEventTimestampBetween(
                    status, status, from, to, pageable);
        } else {
            logs = auditLogRepository.findByEventTimestampBetween(from, to, pageable);
        }

        long errorCount = auditLogRepository.countErrorsSince(LocalDateTime.now().minusHours(1));
        Double avgResponseTime = auditLogRepository.averageExecutionTimeSince(LocalDateTime.now().minusHours(1));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("logs", logs.getContent());
        result.put("totalPages", logs.getTotalPages());
        result.put("totalElements", logs.getTotalElements());
        result.put("errorCountLastHour", errorCount);
        result.put("avgResponseTimeMs", avgResponseTime != null ? avgResponseTime : 0.0);

        return ResponseEntity.ok(result);
    }

    private LocalDateTime resolveTimeframe(String timeframe) {
        return switch (timeframe.toLowerCase()) {
            case "7d" -> LocalDateTime.now().minusDays(7);
            case "30d" -> LocalDateTime.now().minusDays(30);
            case "ytd" -> LocalDateTime.of(LocalDate.now().withDayOfYear(1), LocalTime.MIN);
            default -> null;
        };
    }
}
