package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Monthly trend entry for income and expense")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendEntryDTO {

    @Schema(description = "Month label", example = "Jun 2026")
    private String month;

    @Schema(description = "Total income for the month", example = "12000.00")
    private BigDecimal totalIncome;

    @Schema(description = "Total expense for the month", example = "9000.00")
    private BigDecimal totalExpense;
}
