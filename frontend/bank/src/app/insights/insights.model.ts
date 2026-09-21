export interface TrendEntry {
  month: string;
  totalIncome: number;
  totalExpense: number;
}

export interface FinancialInsights {
  totalIncome: number;
  totalExpense: number;
  savings: number;
  trendSummary: TrendEntry[];
}
