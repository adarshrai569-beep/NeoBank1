package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Aggregated financial insights for a user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialInsightsDTO {

    @Schema(description = "Sum of all credit transactions", example = "25000.00")
    private BigDecimal totalIncome;

    @Schema(description = "Sum of all debit transactions", example = "18500.00")
    private BigDecimal totalExpense;

    @Schema(description = "Savings computed as income minus expense", example = "6500.00")
    private BigDecimal savings;

    @Schema(description = "Monthly income and expense trend summary")
    private List<TrendEntryDTO> trendSummary;
}
