package com.bank.controller;

import com.bank.dto.*;
import com.bank.service.LoanApplicationService;
import com.bank.util.AuditLogger;
import com.bank.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final SecurityUtil securityUtil;
    private final AuditLogger auditLogger;

    @PostMapping("/apply")
    public ResponseEntity<?> applyLoan(@RequestBody LoanApplicationRequestDTO request) {
        return ResponseEntity.status(201).body(loanApplicationService.apply(request));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications() {
        return ResponseEntity.ok(loanApplicationService.getMyApplications());
    }

    @GetMapping("/admin/applications")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllApplications(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(loanApplicationService.getAllApplications(status));
    }

    @PutMapping("/{id}/decision")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> decideLoan(@PathVariable Long id, @RequestBody LoanDecisionDTO decision) {
        ResponseEntity<?> response = ResponseEntity.ok(loanApplicationService.decide(id, decision));
        String action = "LOAN_DECISION_" + (decision.getDecision() == null ? "UNKNOWN" : decision.getDecision());
        auditLogger.logAdminAction(securityUtil.getUserId(), action, "LOAN_APPLICATION", id);
        return response;
    }

    // ✅ EMI API
    @GetMapping("/{id}/emi")
    public ResponseEntity<?> getEMI(@PathVariable Long id) {
        return ResponseEntity.ok(loanApplicationService.calculateEMI(id));
    }
}
