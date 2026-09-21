package com.bank.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class EmiCalculatorUtil {

    private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

    private EmiCalculatorUtil() {
    }

    public static BigDecimal calculateEmi(BigDecimal principal, double annualRate, int tenureMonths) {
        if (principal == null || tenureMonths <= 0 || annualRate <= 0) {
            throw new IllegalArgumentException("Invalid EMI inputs");
        }

        double monthlyRate = annualRate / 12.0 / 100.0;
        double factor = Math.pow(1 + monthlyRate, tenureMonths);
        double emi = principal.doubleValue() * monthlyRate * factor / (factor - 1);

        return BigDecimal.valueOf(emi).round(MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP);
    }
}
