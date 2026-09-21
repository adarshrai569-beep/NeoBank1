package com.bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String mobile;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String accountType; // SAVINGS / CURRENT

    @Column(nullable = false)
    private String idType; // AADHAR / PAN

    @Column(nullable = false, unique = true)
    private String idNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ✅ Activate immediately (can be changed back later)
    @Column(nullable = false)
    private Boolean isActive = true;

    // ✅ ALLOW DASHBOARD ACCESS
    @Column(nullable = false)
    private Boolean approved = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, unique = true)
    private String customerId;

    @OneToMany(
        mappedBy = "user",
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL
    )
    @JsonIgnoreProperties("user")
    private List<Account> accounts;
}