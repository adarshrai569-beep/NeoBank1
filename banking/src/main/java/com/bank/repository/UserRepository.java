package com.bank.repository;

import com.bank.entity.Role;
import com.bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ THIS IS REQUIRED
    boolean existsByRole(Role role);
}

