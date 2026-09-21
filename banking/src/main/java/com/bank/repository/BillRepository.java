package com.bank.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.entity.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.userId = :userId AND LOWER(b.billerName) = LOWER(:billerName) AND b.status = 'PAID' AND b.dueDate >= :startDate AND b.dueDate < :endDate")
    double sumPaidBillsByCategory(@Param("userId") Long userId, @Param("billerName") String billerName, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}