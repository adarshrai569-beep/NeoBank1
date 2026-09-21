package com.bank.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LoanEMIResponseDTO {

    private BigDecimal principal;
    private double annualInterestRate;
    private int tenureMonths;

    private BigDecimal monthlyEMI;
    private BigDecimal totalAmount;
    private BigDecimal totalInterest;
}
