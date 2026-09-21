package com.bank.repository;

import com.bank.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {

    List<LoanAccount> findByUserId(Long userId);

    @Query("SELECT COUNT(la) FROM LoanAccount la WHERE la.id IN (SELECT DISTINCT lr.loanAccount.id FROM LoanRepayment lr WHERE lr.paymentStatus = 'OVERDUE')")
    long countOverdueLoans();
}
