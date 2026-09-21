package com.bank.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LoanApplicationRequestDTO {

    private Long productId;
    private BigDecimal amount;
    private Integer tenure;
}