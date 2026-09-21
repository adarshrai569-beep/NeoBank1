package com.bank.service.impl;

import java.time.LocalDate;
import java.util.List;
import com.bank.service.BudgetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.dto.BudgetDTO;
import com.bank.entity.Budget;
import com.bank.repository.BudgetRepository;
import com.bank.repository.BillRepository;
import com.bank.service.BudgetService;
import com.bank.util.SecurityUtil;
import com.bank.entity.Category;   // ✅ CORRECT
@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository repo;

    @Autowired
    private BillRepository billRepo;

    @Autowired
    private SecurityUtil securityUtil;

    // ✅ CREATE
    @Override
    public Budget create(Budget budget) {

        Long userId = securityUtil.getUserId();

        if (budget.getLimitAmount() == null || budget.getLimitAmount() <= 0) {
            throw new RuntimeException("Limit must be greater than 0");
        }

        if (budget.getBudgetMonth() == null) {
            throw new RuntimeException("Budget month is required");
        }

        boolean exists = repo.existsByUserIdAndCategoryAndBudgetMonth(
                userId,
                budget.getCategory(),
                budget.getBudgetMonth()
        );

        if (exists) {
            throw new RuntimeException("Budget already exists");
        }

        budget.setUserId(userId);

        return repo.save(budget);
    }

    // ✅ GET RAW DATA
    @Override
    public List<Budget> getByMonth(LocalDate month) {

        Long userId = securityUtil.getUserId();

        return repo.findByUserIdAndBudgetMonth(userId, month);
    }

    // ✅ ✅ ADD THIS METHOD HERE (Sprint 13 Feature)
    @Override
    public List<BudgetDTO> getBudgetSummary(LocalDate month) {

        Long userId = securityUtil.getUserId();

        List<Budget> budgets = repo.findByUserIdAndBudgetMonth(userId, month);

        LocalDate startDate = month.withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1);

        return budgets.stream().map(b -> {

            // Sum paid bills where billerName matches category name
            double spent = billRepo.sumPaidBillsByCategory(
                    userId,
                    b.getCategory().name(),
                    startDate,
                    endDate
            );

            double remaining = b.getLimitAmount() - spent;
            double utilization = (spent / b.getLimitAmount()) * 100;

            return new BudgetDTO(
                    b.getId(),
                    b.getCategory(),
                    b.getLimitAmount(),
                    spent,
                    remaining,
                    utilization,
                    b.getBudgetMonth()
            );
        }).toList();
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

}