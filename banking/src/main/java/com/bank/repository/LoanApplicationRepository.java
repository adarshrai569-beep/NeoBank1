package com.bank.repository;

import com.bank.entity.LoanApplication;
import com.bank.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    boolean existsByUserIdAndLoanProduct_IdAndStatus(
            Long userId,
            Long productId,
            LoanStatus status
    );

    List<LoanApplication> findByUserId(Long userId);

    List<LoanApplication> findByStatus(LoanStatus status);

    @Query("SELECT COUNT(la) FROM LoanApplication la WHERE la.status = :status")
    long countByStatus(@Param("status") String status);
}