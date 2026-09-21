package com.bank.repository;

import com.bank.entity.LoanRepayment;
import com.bank.entity.LoanRepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoanAccount_IdOrderByInstalmentNumberAsc(Long loanAccountId);

    List<LoanRepayment> findByLoanAccount_IdAndPaymentStatusOrderByInstalmentNumberAsc(
            Long loanAccountId,
            LoanRepaymentStatus paymentStatus
    );

    @Query("SELECT lr FROM LoanRepayment lr WHERE lr.loanAccount.id = :loanAccountId AND lr.paymentStatus = :status")
    List<LoanRepayment> findByLoanAccountIdAndPaymentStatus(@Param("loanAccountId") Long loanAccountId, @Param("status") String status);
}
