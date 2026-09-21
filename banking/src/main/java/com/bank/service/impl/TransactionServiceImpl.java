package com.bank.service.impl;

import com.bank.entity.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.RewardService;
import com.bank.service.TransactionService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RewardService rewardService;

    private static final BigDecimal DEPOSIT_MIN = new BigDecimal("1000");
    private static final BigDecimal DEPOSIT_MAX_DAILY = new BigDecimal("200000");
    private static final BigDecimal WITHDRAW_MIN = new BigDecimal("1000");
    private static final BigDecimal WITHDRAW_MAX_DAILY = new BigDecimal("100000");
    private static final int MAX_TRANSACTIONS_PER_DAY = 10;

    @Override
    @Transactional
    public Transaction doTransaction(
            Long accountId,
            TransactionType type,
            BigDecimal amount,
            String desc,
            String email) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("Unauthorized access to account");
        }

        if (!account.getIsActive()) {
            throw new RuntimeException("Account pending admin approval");
        }

        if (account.getIsFrozen()) {
            throw new RuntimeException(
                "Your account is frozen. Please contact admin or branch."
            );
        }

        // ===== DAILY TRANSACTION LIMITS =====
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        // Check daily transaction count (max 10 per day)
        long todayCount = transactionRepository.countTodayTransactions(account, startOfDay);
        if (todayCount >= MAX_TRANSACTIONS_PER_DAY) {
            throw new RuntimeException("Daily transaction limit reached (max " + MAX_TRANSACTIONS_PER_DAY + " transactions per day)");
        }

        // Check minimum amount
        if (type == TransactionType.CREDIT && amount.compareTo(DEPOSIT_MIN) < 0) {
            throw new RuntimeException("Minimum deposit amount is ₹1,000");
        }
        if (type == TransactionType.DEBIT && amount.compareTo(WITHDRAW_MIN) < 0) {
            throw new RuntimeException("Minimum withdrawal amount is ₹1,000");
        }

        // Check daily maximum
        BigDecimal todayTotal = transactionRepository.sumTodayAmountByType(account, type, startOfDay);
        BigDecimal dailyMax = (type == TransactionType.CREDIT) ? DEPOSIT_MAX_DAILY : WITHDRAW_MAX_DAILY;
        if (todayTotal.add(amount).compareTo(dailyMax) > 0) {
            BigDecimal remaining = dailyMax.subtract(todayTotal);
            String typeName = (type == TransactionType.CREDIT) ? "deposit" : "withdrawal";
            throw new RuntimeException("Daily " + typeName + " limit is ₹" + dailyMax.toPlainString() + ". Remaining today: ₹" + remaining.toPlainString());
        }

        // ✅ Check for insufficient funds on DEBIT (withdraw)
        if (type == TransactionType.DEBIT && account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance. Available: " + account.getBalance());
        }

        BigDecimal newBalance =
                type == TransactionType.CREDIT
                        ? account.getBalance().add(amount)
                        : account.getBalance().subtract(amount);

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .account(account)
                .type(type)
                .amount(amount)
                .description(desc)
                .balanceAfter(newBalance)
                .build();

        Transaction saved = transactionRepository.save(tx);

        // Award 5 reward points per transaction
        try {
            rewardService.addPoints(account.getUser().getId(), 5);
        } catch (Exception ignored) {}

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(Long accountId, String email) {
        Account account = accountRepository
                .findByIdAndUserEmail(accountId, email)
                .orElseThrow(() -> new AccessDeniedException("Unauthorized access to account"));

        return transactionRepository.findByAccountOrderByTransactionDateDesc(account);
    }
}