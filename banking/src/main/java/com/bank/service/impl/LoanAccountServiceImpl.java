package com.bank.service.impl;

import com.bank.dto.LoanAccountDTO;
import com.bank.entity.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.LoanAccountRepository;
import com.bank.repository.LoanRepaymentRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.service.LoanAccountService;
import com.bank.util.EmiCalculatorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanAccountServiceImpl implements LoanAccountService {

    private static final int SCALE = 2;

    private final LoanAccountRepository loanAccountRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public LoanAccount createAccountAndSchedule(LoanApplication application) {
        BigDecimal principal = application.getAmount();
        double annualRate = application.getLoanProduct().getAnnualInterestRate();
        int tenureMonths = application.getTenure();

        BigDecimal emiAmount = EmiCalculatorUtil.calculateEmi(principal, annualRate, tenureMonths);

        LoanAccount account = LoanAccount.builder()
                .loanApplication(application)
                .userId(application.getUserId())
                .principalAmount(principal)
                .annualInterestRate(annualRate)
                .tenureMonths(tenureMonths)
                .emiAmount(emiAmount)
                .disbursedAt(LocalDateTime.now())
                .build();

        LoanAccount savedAccount = loanAccountRepository.save(account);
        generateRepaymentSchedule(savedAccount);

        // Credit loan amount to user's bank account
        try {
            User user = userRepository.findById(application.getUserId()).orElse(null);
            if (user != null) {
                List<Account> accounts = accountRepository.findByUser(user);
                Account primaryAccount = accounts.stream()
                        .filter(a -> a.getIsActive())
                        .findFirst().orElse(null);
                if (primaryAccount != null) {
                    primaryAccount.setBalance(primaryAccount.getBalance().add(principal));
                    accountRepository.save(primaryAccount);

                    Transaction tx = Transaction.builder()
                            .account(primaryAccount)
                            .type(TransactionType.CREDIT)
                            .amount(principal)
                            .description("Loan Disbursement - " + application.getLoanProduct().getProductName())
                            .balanceAfter(primaryAccount.getBalance())
                            .build();
                    transactionRepository.save(tx);
                }
            }
        } catch (Exception ignored) {}

        return savedAccount;
    }

    @Override
    public List<LoanAccountDTO> getMyAccounts() {
        User user = getCurrentUser();
        return loanAccountRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void generateRepaymentSchedule(LoanAccount account) {
        BigDecimal monthlyRate = BigDecimal.valueOf(account.getAnnualInterestRate())
                .divide(BigDecimal.valueOf(12 * 100.0), 10, RoundingMode.HALF_UP);

        BigDecimal outstanding = account.getPrincipalAmount();
        BigDecimal emi = account.getEmiAmount();
        LocalDate disbursedDate = account.getDisbursedAt().toLocalDate();

        List<LoanRepayment> repayments = new ArrayList<>();

        for (int i = 1; i <= account.getTenureMonths(); i++) {
            BigDecimal interest = outstanding.multiply(monthlyRate).setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal principalComponent;

            if (i == account.getTenureMonths()) {
                principalComponent = outstanding.setScale(SCALE, RoundingMode.HALF_UP);
                interest = emi.subtract(principalComponent).setScale(SCALE, RoundingMode.HALF_UP);
                if (interest.compareTo(BigDecimal.ZERO) < 0) {
                    interest = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
                }
            } else {
                principalComponent = emi.subtract(interest).setScale(SCALE, RoundingMode.HALF_UP);
            }

            outstanding = outstanding.subtract(principalComponent).setScale(10, RoundingMode.HALF_UP);

            repayments.add(LoanRepayment.builder()
                    .loanAccount(account)
                    .instalmentNumber(i)
                    .dueDate(disbursedDate.plusMonths(i))
                    .emiAmount(emi)
                    .principalComponent(principalComponent)
                    .interestComponent(interest)
                    .paymentStatus(LoanRepaymentStatus.PENDING)
                    .build());
        }

        loanRepaymentRepository.saveAll(repayments);
    }

    private LoanAccountDTO mapToDTO(LoanAccount account) {
        return LoanAccountDTO.builder()
                .id(account.getId())
                .loanApplicationId(account.getLoanApplication().getId())
                .userId(account.getUserId())
                .productName(account.getLoanApplication().getLoanProduct().getProductName())
                .principalAmount(account.getPrincipalAmount())
                .annualInterestRate(account.getAnnualInterestRate())
                .tenureMonths(account.getTenureMonths())
                .emiAmount(account.getEmiAmount())
                .disbursedAt(account.getDisbursedAt())
                .build();
    }

    private User getCurrentUser() {
        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
