package com.bank.service;

import com.bank.entity.Account;
import com.bank.entity.AccountType;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    Account createAccount(AccountType type, String email);
    List<Account> getMyAccounts(String email);
    Account getAccount(Long id, String email);
    void transferByAccountNumber(
            Long fromAccountId,
            String toAccountNumber,
            String toCustomerId,
            BigDecimal amount,
            String description,
            String senderEmail
    );
    
}