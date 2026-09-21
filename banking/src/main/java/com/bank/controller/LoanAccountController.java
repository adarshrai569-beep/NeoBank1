package com.bank.controller;

import com.bank.service.LoanAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanAccountController {

    private final LoanAccountService loanAccountService;

    @GetMapping("/my-accounts")
    public ResponseEntity<?> getMyAccounts() {
        return ResponseEntity.ok(loanAccountService.getMyAccounts());
    }
}
