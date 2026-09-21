package com.bank.service.impl;

import com.bank.entity.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.service.AccountService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository; // ✅ REQUIRED
    private final UserRepository userRepository;

    private static final BigDecimal TRANSFER_MIN = new BigDecimal("1000");
    private static final BigDecimal TRANSFER_MAX_DAILY = new BigDecimal("100000");
    private static final int MAX_TRANSACTIONS_PER_DAY = 10;

    // ✅ CREATE ACCOUNT
    @Override
    public Account createAccount(AccountType type, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        Account account = Account.builder()
                .user(user)
                .accountType(type)
                .accountNumber(UUID.randomUUID().toString().substring(0, 12))
                .balance(BigDecimal.ZERO)
                .isActive(false) // ✅ admin approval required
                .build();

        return accountRepository.save(account);
    }

    // ✅ GET LOGGED-IN USER ACCOUNTS
    @Override
    public List<Account> getMyAccounts(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        if (!user.getApproved()) {
            return List.of(); // customers wait for approval
        }

        return accountRepository.findByUserEmail(email)
                .stream()
                .filter(Account::getIsActive)
                .collect(Collectors.toList());
    }

    // ✅ GET SINGLE ACCOUNT WITH OWNERSHIP CHECK
    @Override
    public Account getAccount(Long id, String email) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!acc.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("Unauthorized access");
        }

        if (!acc.getIsActive()) {
            throw new RuntimeException("Account pending admin approval");
        }

        return acc;
    }

    // ✅ ACCOUNT → ACCOUNT TRANSFER (ACCOUNT NUMBER + CUSTOMER ID)
    @Transactional
    @Override
    public void transferByAccountNumber(
            Long fromAccountId,
            String toAccountNumber,
            String toCustomerId,
            BigDecimal amount,
            String description,
            String senderEmail
    ) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        // ===== TRANSFER LIMITS =====
        if (amount.compareTo(TRANSFER_MIN) < 0) {
            throw new RuntimeException("Minimum transfer amount is ₹1,000");
        }

        // ✅ Sender validation
        Account fromAccount = getAccount(fromAccountId, senderEmail);

        // Check daily transaction count
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCount = transactionRepository.countTodayTransactions(fromAccount, startOfDay);
        if (todayCount >= MAX_TRANSACTIONS_PER_DAY) {
            throw new RuntimeException("Daily transaction limit reached (max " + MAX_TRANSACTIONS_PER_DAY + " transactions per day)");
        }

        // Check daily transfer limit
        BigDecimal todayTransferred = transactionRepository.sumTodayAmountByType(fromAccount, TransactionType.DEBIT, startOfDay);
        if (todayTransferred.add(amount).compareTo(TRANSFER_MAX_DAILY) > 0) {
            BigDecimal remaining = TRANSFER_MAX_DAILY.subtract(todayTransferred);
            throw new RuntimeException("Daily transfer limit is ₹1,00,000. Remaining today: ₹" + remaining.toPlainString());
        }

        if (fromAccount.getIsFrozen()) {
            throw new RuntimeException("Sender account is frozen");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // ✅ Receiver validation (account number + customer ID)
        Account toAccount = accountRepository
                .findByAccountNumberAndUser_CustomerId(
                        toAccountNumber, toCustomerId
                )
                .orElseThrow(() ->
                        new RuntimeException("Invalid recipient account details")
                );

        if (!toAccount.getIsActive()) {
            throw new RuntimeException("Recipient account not active");
        }

        if (toAccount.getIsFrozen()) {
            throw new RuntimeException("Recipient account is frozen");
        }

        // ✅ Debit sender
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        Transaction debitTx = Transaction.builder()
                .account(fromAccount)
                .type(TransactionType.DEBIT)
                .amount(amount)
                .description("Transfer to " + toAccountNumber + " : " + description)
                .balanceAfter(fromAccount.getBalance())
                .build();

        transactionRepository.save(debitTx);

        // ✅ Credit receiver
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        Transaction creditTx = Transaction.builder()
                .account(toAccount)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .description("Transfer from " + fromAccount.getAccountNumber())
                .balanceAfter(toAccount.getBalance())
                .build();

        transactionRepository.save(creditTx);
    }
}
