package com.bank.repository;

import com.bank.entity.Account;
import com.bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserEmail(String email);
    Optional<Account> findByIdAndUserEmail(Long id, String email);
    
    // ✅ ADD this method for AdminController
    List<Account> findByUser(User user);
    
    // ✅ ADD this method for AccountService
    List<Account> findByUserAndIsActiveTrue(User user);
    
    // ✅ ADD this alternative method that might be more useful
    List<Account> findByUserAndIsActive(User user, Boolean isActive);
    Optional<Account> findByAccountNumberAndUser_CustomerId(
            String accountNumber,
            String customerId
    );
}