package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Platform-wide admin dashboard metrics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {

    @Schema(description = "Total registered users", example = "150")
    private Long totalUsers;

    @Schema(description = "Total active users", example = "140")
    private Long totalActiveUsers;

    @Schema(description = "Total loan applications", example = "45")
    private Long totalLoans;

    @Schema(description = "Count of pending approvals", example = "3")
    private Long pendingApprovals;

    @Schema(description = "Total transactions across platform", example = "620")
    private Long totalTransactions;

    @Schema(description = "Platform savings rate as percentage", example = "12.50")
    private BigDecimal platformSavingsRate;

    @Schema(description = "Total bank accounts", example = "180")
    private Long totalAccounts;

    @Schema(description = "Total balance across all accounts", example = "5000000.00")
    private BigDecimal totalBalance;
}
