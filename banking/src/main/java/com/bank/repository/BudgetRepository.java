package com.bank.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.entity.Budget;
import com.bank.entity.Category;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    boolean existsByUserIdAndCategoryAndBudgetMonth(
        Long userId, Category category, LocalDate month);

    List<Budget> findByUserIdAndBudgetMonth(
        Long userId, LocalDate month);
}
