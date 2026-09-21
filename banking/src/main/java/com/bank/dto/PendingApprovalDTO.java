package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Pending approval item details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingApprovalDTO {

    @Schema(description = "Approval item id", example = "12")
    private Long id;

    @Schema(description = "Approval item type", example = "LOAN_APPLICATION")
    private String type;

    @Schema(description = "Applicant full name", example = "Rohan Sharma")
    private String applicantName;

    @Schema(description = "Product or item name", example = "Home Loan")
    private String productName;

    @Schema(description = "Requested amount", example = "250000.00")
    private BigDecimal requestedAmount;

    @Schema(description = "When the item was submitted")
    private LocalDateTime appliedAt;
}
