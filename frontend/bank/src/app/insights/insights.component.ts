import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import { AuthService } from '../auth/auth.service';
import { FinancialInsights } from './insights.model';
import { InsightsService } from './insights.service';

Chart.register(...registerables);

@Component({
  selector: 'app-insights',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './insights.component.html',
  styleUrls: ['./insights.component.css']
})
export class InsightsComponent implements OnInit {

  isLoading = true;
  errorMessage = '';
  insights: FinancialInsights | null = null;

  chartType: 'bar' = 'bar';
  chartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      {
        label: 'Income',
        data: [],
        backgroundColor: 'rgba(46, 125, 50, 0.7)',
        borderColor: '#2E7D32'
      },
      {
        label: 'Expense',
        data: [],
        backgroundColor: 'rgba(198, 40, 40, 0.7)',
        borderColor: '#C62828'
      }
    ]
  };

  chartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value: number | string) => `₹${value}`
        }
      }
    },
    plugins: {
      legend: {
        position: 'top'
      }
    }
  };

  constructor(
    private authService: AuthService,
    private insightsService: InsightsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.errorMessage = 'Please log in to view insights.';
      this.isLoading = false;
      return;
    }

    this.insightsService.getInsights(userId).subscribe({
      next: (data) => {
        this.insights = data;
        this.syncChart(data);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load insights right now.';
        this.isLoading = false;
      }
    });
  }

  formatCurrency(value: number | null | undefined): string {
    const amount = value ?? 0;
    return `₹${amount.toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })}`;
  }

  getSavingsClass(): string {
    if (!this.insights) return '';
    return this.insights.savings < 0 ? 'negative' : 'positive';
  }

  get savingsNegative(): boolean {
    return (this.insights?.savings ?? 0) < 0;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  private syncChart(data: FinancialInsights): void {
    const labels = data.trendSummary.map(entry => entry.month);
    const income = data.trendSummary.map(entry => entry.totalIncome ?? 0);
    const expense = data.trendSummary.map(entry => entry.totalExpense ?? 0);

    this.chartData = {
      ...this.chartData,
      labels,
      datasets: [
        { ...this.chartData.datasets[0], data: income },
        { ...this.chartData.datasets[1], data: expense }
      ]
    };
  }
}
