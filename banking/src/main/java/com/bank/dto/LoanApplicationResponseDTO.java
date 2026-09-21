package com.bank.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class LoanApplicationResponseDTO {

    private Long applicationId;
    private Long userId;
    private String productName;
    private BigDecimal amount;
    private Integer tenure;
    private String status;
    private String adminRemarks;
    private LocalDateTime appliedAt;
    private LocalDateTime decidedAt;
}
