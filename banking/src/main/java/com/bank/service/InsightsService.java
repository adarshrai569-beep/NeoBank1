package com.bank.service;

import com.bank.dto.FinancialInsightsDTO;

public interface InsightsService {

    FinancialInsightsDTO getInsights(Long userId);
}
