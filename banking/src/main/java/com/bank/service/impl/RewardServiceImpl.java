package com.bank.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.dto.RewardDTO;
import com.bank.entity.Reward;
import com.bank.repository.RewardRepository;

import com.bank.service.RewardService;

import jakarta.transaction.Transactional;
import com.bank.entity.RewardHistory;   // ✅ correct
import com.bank.repository.RewardHistoryRepository;


@Service
public class RewardServiceImpl implements RewardService {

    @Autowired
    private RewardRepository repo;

    @Autowired
    private RewardHistoryRepository historyRepo; // ✅ FIXED

    @Override
    public Reward get(Long userId) {

        return repo.findByUserId(userId)
                .orElseGet(() -> {
                    Reward r = new Reward();
                    r.setUserId(userId);
                    r.setPointsBalance(0);
                    return repo.save(r);
                });
    }

    @Override
    @Transactional
    public void addPoints(Long userId, int points) {

        Reward r = get(userId);

        int newBalance = r.getPointsBalance() + points;

        if (newBalance < 0) {
            throw new RuntimeException("Points cannot be negative");
        }

        r.setPointsBalance(newBalance);
        repo.save(r);

        // ✅ Save history
        RewardHistory h = RewardHistory.builder()
                .userId(userId)
                .points(points)
                .reason("Bill Payment Reward")
                .date(LocalDate.now())
                .build();

        historyRepo.save(h); // ✅ NOW WORKS
    }

	@Override
	public RewardDTO getFullDetails(Long userId) {
		Reward reward = get(userId);
		List<RewardHistory> historyList = historyRepo.findByUserIdOrderByDateDesc(userId);

		List<String> historyStrings = historyList.stream()
				.map(h -> h.getDate() + " | " + h.getReason() + " | +" + h.getPoints() + " pts")
				.toList();

		return RewardDTO.builder()
				.userId(userId)
				.balance(reward.getPointsBalance())
				.history(historyStrings)
				.build();
	}
}
