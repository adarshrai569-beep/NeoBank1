package com.bank.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardHistory {

    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    private int points;

    private String reason;

    private LocalDate date;
}
