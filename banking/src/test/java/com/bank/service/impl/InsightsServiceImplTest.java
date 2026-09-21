package com.bank.service.impl;

import com.bank.dto.FinancialInsightsDTO;
import com.bank.dto.TrendEntryDTO;
import com.bank.entity.TransactionType;
import com.bank.repository.InsightsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightsServiceImplTest {

    @Mock
    private InsightsRepository insightsRepository;

    private InsightsServiceImpl insightsService;

    @BeforeEach
    void setUp() {
        insightsService = new InsightsServiceImpl(insightsRepository);
    }

    @Test
    void getInsightsCalculatesSavingsAndTrendSize() {
        when(insightsRepository.sumByUserAndType(1L, TransactionType.CREDIT))
                .thenReturn(new BigDecimal("1000.00"));
        when(insightsRepository.sumByUserAndType(1L, TransactionType.DEBIT))
                .thenReturn(new BigDecimal("1500.00"));
        when(insightsRepository.findTrendSummary(eq(1L), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        FinancialInsightsDTO result = insightsService.getInsights(1L);

        assertThat(result.getSavings()).isEqualByComparingTo(new BigDecimal("-500.00"));
        assertThat(result.getTrendSummary()).hasSize(6);
    }

    @Test
    void buildTrendSummaryPadsMissingMonthsWithZeros() {
        YearMonth startMonth = YearMonth.now().minusMonths(5);
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{startMonth.getYear(), startMonth.getMonthValue(),
                new BigDecimal("120.00"), new BigDecimal("80.00")});

        when(insightsRepository.sumByUserAndType(1L, TransactionType.CREDIT))
                .thenReturn(BigDecimal.ZERO);
        when(insightsRepository.sumByUserAndType(1L, TransactionType.DEBIT))
                .thenReturn(BigDecimal.ZERO);
        when(insightsRepository.findTrendSummary(eq(1L), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(rows);

        FinancialInsightsDTO result = insightsService.getInsights(1L);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

        TrendEntryDTO first = result.getTrendSummary().get(0);
        assertThat(first.getMonth()).isEqualTo(startMonth.format(formatter));
        assertThat(first.getTotalIncome()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(first.getTotalExpense()).isEqualByComparingTo(new BigDecimal("80.00"));

        TrendEntryDTO second = result.getTrendSummary().get(1);
        assertThat(second.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(second.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
