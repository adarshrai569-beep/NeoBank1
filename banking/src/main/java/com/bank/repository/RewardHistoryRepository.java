package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.entity.RewardHistory;

import java.util.List;

public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {
    List<RewardHistory> findByUserIdOrderByDateDesc(Long userId);
}