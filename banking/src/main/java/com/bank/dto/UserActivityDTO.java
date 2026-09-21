package com.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Recent user activity for admin review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDTO {

    @Schema(description = "Most recent transactions")
    private List<TransactionActivityDTO> recentTransactions;

    @Schema(description = "Most recent login timestamps")
    private List<LocalDateTime> recentLogins;
}
