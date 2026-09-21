package com.bank.service;

import java.time.LocalDate;
import java.util.List;

import com.bank.entity.Budget;
import com.bank.dto.BudgetDTO;

public interface BudgetService {

    Budget create(Budget budget);

    List<Budget> getByMonth(LocalDate month);

    // ✅ ADD THIS
    List<BudgetDTO> getBudgetSummary(LocalDate month);

    void deleteById(Long id);
}