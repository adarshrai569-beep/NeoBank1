package com.bank.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductDTO {

    private Long id;
    private String productName;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private double annualInterestRate;
    private List<Integer> allowedTenures;
}