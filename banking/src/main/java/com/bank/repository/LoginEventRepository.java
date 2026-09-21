package com.bank.repository;

import com.bank.entity.LoginEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginEventRepository extends JpaRepository<LoginEvent, Long> {

    List<LoginEvent> findByUserIdOrderByLoginAtDesc(Long userId, Pageable pageable);
}
