package com.bank.service.impl;

import com.bank.dto.RepaymentScheduleDTO;
import com.bank.entity.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.LoanAccountRepository;
import com.bank.repository.LoanRepaymentRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.service.LoanRepaymentService;
import com.bank.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class LoanRepaymentServiceImpl implements LoanRepaymentService {

    private final LoanAccountRepository loanAccountRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<RepaymentScheduleDTO> getSchedule(Long loanAccountId, String status) {
        LoanAccount account = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Loan account not found"));

        if (!canAccessAccount(account)) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        updateOverdueStatuses(loanAccountId);

        List<LoanRepayment> repayments;
        if (status == null || status.isBlank()) {
            repayments = loanRepaymentRepository
                    .findByLoanAccount_IdOrderByInstalmentNumberAsc(loanAccountId);
        } else {
            try {
                LoanRepaymentStatus parsed = LoanRepaymentStatus.valueOf(status.toUpperCase());
                repayments = loanRepaymentRepository
                        .findByLoanAccount_IdAndPaymentStatusOrderByInstalmentNumberAsc(loanAccountId, parsed);
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(BAD_REQUEST, "Invalid repayment status");
            }
        }

        return repayments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public RepaymentScheduleDTO markPaid(Long loanAccountId, Long repaymentId) {
        LoanAccount account = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Loan account not found"));

        if (!canAccessAccount(account)) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        LoanRepayment repayment = loanRepaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Repayment not found"));

        if (!repayment.getLoanAccount().getId().equals(loanAccountId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Repayment does not belong to account");
        }

        if (repayment.getPaymentStatus() == LoanRepaymentStatus.PAID) {
            throw new ResponseStatusException(BAD_REQUEST, "Repayment already paid");
        }

        repayment.setPaymentStatus(LoanRepaymentStatus.PAID);
        repayment.setPaidAt(LocalDateTime.now());

        // Deduct EMI amount from user's bank account
        try {
            User user = userRepository.findById(account.getUserId()).orElse(null);
            if (user != null) {
                List<Account> accounts = accountRepository.findByUser(user);
                Account primaryAccount = accounts.stream()
                        .filter(a -> a.getIsActive())
                        .findFirst().orElse(null);
                if (primaryAccount != null) {
                    java.math.BigDecimal emiAmount = repayment.getEmiAmount();
                    if (primaryAccount.getBalance().compareTo(emiAmount) >= 0) {
                        primaryAccount.setBalance(primaryAccount.getBalance().subtract(emiAmount));
                        accountRepository.save(primaryAccount);

                        Transaction tx = Transaction.builder()
                                .account(primaryAccount)
                                .type(TransactionType.DEBIT)
                                .amount(emiAmount)
                                .description("Loan EMI Payment - Instalment #" + repayment.getInstalmentNumber())
                                .balanceAfter(primaryAccount.getBalance())
                                .build();
                        transactionRepository.save(tx);
                    } else {
                        throw new ResponseStatusException(BAD_REQUEST, "Insufficient balance for EMI payment. Need ₹" + emiAmount);
                    }
                }
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception ignored) {}

        // Award 20 reward points per EMI paid
        try {
            rewardService.addPoints(account.getUserId(), 20);
        } catch (Exception ignored) {}

        return mapToDTO(loanRepaymentRepository.save(repayment));
    }

    private void updateOverdueStatuses(Long loanAccountId) {
        LocalDate today = LocalDate.now();
        List<LoanRepayment> repayments = loanRepaymentRepository
                .findByLoanAccount_IdOrderByInstalmentNumberAsc(loanAccountId);

        boolean changed = false;
        for (LoanRepayment repayment : repayments) {
            if (repayment.getPaymentStatus() == LoanRepaymentStatus.PENDING
                    && repayment.getDueDate().isBefore(today)) {
                repayment.setPaymentStatus(LoanRepaymentStatus.OVERDUE);
                changed = true;
            }
        }

        if (changed) {
            loanRepaymentRepository.saveAll(repayments);
        }
    }

    private boolean canAccessAccount(LoanAccount account) {
        return isAdmin() || getCurrentUserId().equals(account.getUserId());
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Long getCurrentUserId() {
        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"))
                .getId();
    }

    private RepaymentScheduleDTO mapToDTO(LoanRepayment repayment) {
        return RepaymentScheduleDTO.builder()
                .id(repayment.getId())
                .instalmentNumber(repayment.getInstalmentNumber())
                .dueDate(repayment.getDueDate())
                .emiAmount(repayment.getEmiAmount())
                .principalComponent(repayment.getPrincipalComponent())
                .interestComponent(repayment.getInterestComponent())
                .paymentStatus(repayment.getPaymentStatus().name())
                .build();
    }
}
