package com.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bank.dto.RewardDTO;
import com.bank.entity.Reward;
import com.bank.service.RewardService;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    @Autowired
    private RewardService service;

    @GetMapping("/{userId}")
    public RewardDTO get(@PathVariable Long userId) {
        return service.getFullDetails(userId);
    }
}