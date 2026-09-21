package com.bank.config;

import com.bank.entity.LoanProduct;
import com.bank.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoanProductSeeder implements CommandLineRunner {

    private final LoanProductRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 1) return; // Already seeded

        seedProduct("Home Loan", 100000, 5000000, 8.5, List.of(12, 24, 36, 60, 120, 180));
        seedProduct("Personal Loan", 50000, 500000, 10.5, List.of(6, 12, 24, 36));
        seedProduct("Education Loan", 100000, 2000000, 7.0, List.of(12, 24, 36, 48, 60));
        seedProduct("Car Loan", 200000, 1500000, 9.0, List.of(12, 24, 36, 48, 60));
        seedProduct("Gold Loan", 10000, 1000000, 8.0, List.of(6, 12, 24));
        seedProduct("Business Loan", 500000, 5000000, 11.0, List.of(12, 24, 36, 48, 60));
    }

    private void seedProduct(String name, long min, long max, double rate, List<Integer> tenures) {
        if (repository.findByProductName(name).isPresent()) return;

        LoanProduct product = LoanProduct.builder()
                .productName(name)
                .minAmount(BigDecimal.valueOf(min))
                .maxAmount(BigDecimal.valueOf(max))
                .annualInterestRate(rate)
                .allowedTenures(tenures)
                .build();

        repository.save(product);
    }
}
