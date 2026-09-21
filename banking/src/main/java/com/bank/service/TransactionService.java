package com.bank.service;

import com.bank.entity.Transaction;
import com.bank.entity.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    
    Transaction doTransaction(
            Long accountId,
            TransactionType type,
            BigDecimal amount,
            String description,
            String email
    );
    
    List<Transaction> getTransactions(Long accountId, String email);
}