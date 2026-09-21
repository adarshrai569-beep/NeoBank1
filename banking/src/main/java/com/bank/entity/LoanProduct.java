package com.bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(
        name = "loan_products",
        uniqueConstraints = @UniqueConstraint(columnNames = "product_name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private BigDecimal minAmount;

    @Column(nullable = false)
    private BigDecimal maxAmount;

    @Column(nullable = false)
    private double annualInterestRate;

    // ✅ ✅ FIX APPLIED HERE (VERY IMPORTANT)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_product_tenures",
            joinColumns = @JoinColumn(name = "loan_product_id")
    )
    @Column(name = "tenure")
    private List<Integer> allowedTenures;
}