package com.bank.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RewardDTO {

    private Long userId;
    private int balance;
    private List<String> history;
}