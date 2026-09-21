package com.bank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.entity.Reward;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    Optional<Reward> findByUserId(Long userId);
}