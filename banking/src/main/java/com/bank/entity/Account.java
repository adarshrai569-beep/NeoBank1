package com.bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    // ✅ REQUIRED FOR ADMIN APPROVAL
    @Column(nullable = false)
    private Boolean isActive = false;

    private LocalDateTime createdAt = LocalDateTime.now();

 // ✅ FREEZE FLAG
    @Column(nullable = false)
    private Boolean isFrozen = false;

    @PrePersist
    public void prePersist() {
        if (isActive == null) isActive = false;
        if (isFrozen == null) isFrozen = false;
    }
    
}