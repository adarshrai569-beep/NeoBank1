package com.bank.service.impl;

import com.bank.dto.FinancialInsightsDTO;
import com.bank.dto.TrendEntryDTO;
import com.bank.entity.TransactionType;
import com.bank.repository.InsightsRepository;
import com.bank.service.InsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InsightsServiceImpl implements InsightsService {

    private final InsightsRepository insightsRepository;

    @Override
    @Transactional(readOnly = true)
    public FinancialInsightsDTO getInsights(Long userId) {
        BigDecimal totalIncome = defaultZero(
                insightsRepository.sumByUserAndType(userId, TransactionType.CREDIT)
        );
        BigDecimal totalExpense = defaultZero(
                insightsRepository.sumByUserAndType(userId, TransactionType.DEBIT)
        );
        BigDecimal savings = totalIncome.subtract(totalExpense);

        List<TrendEntryDTO> trendSummary = buildTrendSummary(userId);

        return FinancialInsightsDTO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .savings(savings)
                .trendSummary(trendSummary)
                .build();
    }

    private List<TrendEntryDTO> buildTrendSummary(Long userId) {
        YearMonth startMonth = YearMonth.now().minusMonths(5);
        LocalDateTime startDate = startMonth.atDay(1).atStartOfDay();

        List<Object[]> rows = insightsRepository.findTrendSummary(userId, startDate);
        Map<YearMonth, BigDecimal[]> summaryMap = new HashMap<>();

        for (Object[] row : rows) {
            Integer year = (Integer) row[0];
            Integer month = (Integer) row[1];
            BigDecimal income = defaultZero((BigDecimal) row[2]);
            BigDecimal expense = defaultZero((BigDecimal) row[3]);

            summaryMap.put(YearMonth.of(year, month), new BigDecimal[]{income, expense});
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        List<TrendEntryDTO> result = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            YearMonth month = startMonth.plusMonths(i);
            BigDecimal[] values = summaryMap.getOrDefault(
                    month,
                    new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO}
            );

            result.add(TrendEntryDTO.builder()
                    .month(month.format(formatter))
                    .totalIncome(values[0])
                    .totalExpense(values[1])
                    .build());
        }

        return result;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
