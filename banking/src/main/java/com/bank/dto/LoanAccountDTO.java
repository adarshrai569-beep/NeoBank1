package com.bank.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class LoanAccountDTO {

    private Long id;
    private Long loanApplicationId;
    private Long userId;
    private String productName;
    private BigDecimal principalAmount;
    private double annualInterestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private LocalDateTime disbursedAt;
}
