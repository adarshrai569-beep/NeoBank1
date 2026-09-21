package com.bank.repository;

import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account);

    Page<Transaction> findByAccount_User_IdOrderByTransactionDateDesc(Long userId, Pageable pageable);

    // Sprint 5: Admin Analytics
    @Query("SELECT FUNCTION('DATE', t.transactionDate), COUNT(t), SUM(t.amount) FROM Transaction t WHERE t.transactionDate >= :from GROUP BY FUNCTION('DATE', t.transactionDate) ORDER BY FUNCTION('DATE', t.transactionDate)")
    List<Object[]> getDailyVolumes(@Param("from") LocalDateTime from);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.transactionDate >= :from")
    Double getAverageTicketSize(@Param("from") LocalDateTime from);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.transactionDate >= :from")
    Double getTotalByType(@Param("type") TransactionType type, @Param("from") LocalDateTime from);

    // Sprint 5: User Analytics - spending by category
    @Query("SELECT t.description, SUM(t.amount) FROM Transaction t JOIN t.account a WHERE a.user.id = :userId AND t.type = 'DEBIT' AND a.isActive = true AND t.transactionDate >= :from GROUP BY t.description")
    List<Object[]> getCategorySpending(@Param("userId") Long userId, @Param("from") LocalDateTime from);

    @Query("SELECT FUNCTION('YEAR', t.transactionDate), FUNCTION('MONTH', t.transactionDate), t.description, SUM(t.amount) FROM Transaction t JOIN t.account a WHERE a.user.id = :userId AND t.type = 'DEBIT' AND a.isActive = true AND t.transactionDate >= :from GROUP BY FUNCTION('YEAR', t.transactionDate), FUNCTION('MONTH', t.transactionDate), t.description ORDER BY FUNCTION('YEAR', t.transactionDate), FUNCTION('MONTH', t.transactionDate)")
    List<Object[]> getMonthlyCategorySpending(@Param("userId") Long userId, @Param("from") LocalDateTime from);

    // Daily transaction limits
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account = :account AND t.transactionDate >= :startOfDay")
    long countTodayTransactions(@Param("account") Account account, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account = :account AND t.type = :type AND t.transactionDate >= :startOfDay")
    java.math.BigDecimal sumTodayAmountByType(@Param("account") Account account, @Param("type") TransactionType type, @Param("startOfDay") LocalDateTime startOfDay);
}