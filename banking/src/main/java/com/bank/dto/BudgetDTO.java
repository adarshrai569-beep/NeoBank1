package com.bank.dto;

import java.time.LocalDate;
import com.bank.entity.Category;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetDTO {

    private Long id;
    private Category category;
    private Double limitAmount;
    private double spentAmount;
    private double remaining;
    private double utilizationPercentage;
    private LocalDate month;
}