package com.bank.repository;

import com.bank.entity.Transaction;
import com.bank.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InsightsRepository extends JpaRepository<Transaction, Long> {

    @Query("select coalesce(sum(t.amount), 0) " +
            "from Transaction t join t.account a " +
            "where a.user.id = :userId and a.isActive = true and t.type = :type")
    BigDecimal sumByUserAndType(
            @Param("userId") Long userId,
            @Param("type") TransactionType type
    );

    @Query("select function('year', t.transactionDate), function('month', t.transactionDate), " +
            "sum(case when t.type = com.bank.entity.TransactionType.CREDIT then t.amount else 0 end), " +
            "sum(case when t.type = com.bank.entity.TransactionType.DEBIT then t.amount else 0 end) " +
            "from Transaction t join t.account a " +
            "where a.user.id = :userId and a.isActive = true and t.transactionDate >= :startDate " +
            "group by function('year', t.transactionDate), function('month', t.transactionDate) " +
            "order by function('year', t.transactionDate), function('month', t.transactionDate)")
    List<Object[]> findTrendSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate
    );
}
