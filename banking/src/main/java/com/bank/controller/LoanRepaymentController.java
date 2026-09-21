package com.bank.controller;

import com.bank.service.LoanRepaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanRepaymentController {

    private final LoanRepaymentService loanRepaymentService;

    @GetMapping("/{loanAccountId}/repayments")
    public ResponseEntity<?> getRepayments(
            @PathVariable Long loanAccountId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(loanRepaymentService.getSchedule(loanAccountId, status));
    }

    @PatchMapping("/{loanAccountId}/repayments/{repaymentId}/pay")
    public ResponseEntity<?> markPaid(
            @PathVariable Long loanAccountId,
            @PathVariable Long repaymentId
    ) {
        return ResponseEntity.ok(loanRepaymentService.markPaid(loanAccountId, repaymentId));
    }
}
