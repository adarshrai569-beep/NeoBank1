package com.bank.service;

import com.bank.entity.Reward;
import com.bank.dto.RewardDTO;

public interface RewardService {

    Reward get(Long userId);

    void addPoints(Long userId, int points);

    // ✅ ADD THIS
    RewardDTO getFullDetails(Long userId);
}
