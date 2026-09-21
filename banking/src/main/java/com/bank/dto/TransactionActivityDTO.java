package com.bank.dto;

import com.bank.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Simplified transaction activity entry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionActivityDTO {

    @Schema(description = "Transaction id", example = "101")
    private Long id;

    @Schema(description = "Transaction type", example = "CREDIT")
    private TransactionType type;

    @Schema(description = "Transaction amount", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "Transaction date and time")
    private LocalDateTime transactionDate;

    @Schema(description = "Optional description", example = "Salary")
    private String description;
}
