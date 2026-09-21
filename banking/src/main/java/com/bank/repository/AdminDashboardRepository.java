package com.bank.repository;

import com.bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface AdminDashboardRepository extends JpaRepository<User, Long> {

    @Query("select count(u) from User u")
    Long countUsers();

    @Query("select count(u) from User u where u.isActive = true")
    Long countActiveUsers();

    @Query("select count(l) from LoanApplication l")
    Long countLoans();

    @Query("select count(l) from LoanApplication l where l.status = com.bank.entity.LoanStatus.PENDING")
    Long countPendingLoans();

    @Query("select count(t) from Transaction t")
    Long countTransactions();

    @Query("select coalesce(sum(t.amount), 0) from Transaction t join t.account a " +
            "where a.isActive = true and t.type = com.bank.entity.TransactionType.CREDIT")
    BigDecimal sumPlatformIncome();

    @Query("select coalesce(sum(t.amount), 0) from Transaction t join t.account a " +
            "where a.isActive = true and t.type = com.bank.entity.TransactionType.DEBIT")
    BigDecimal sumPlatformExpense();
}
