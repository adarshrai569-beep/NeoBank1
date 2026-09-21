import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BudgetService } from '../user/budget.service';

@Component({
  selector: 'app-budget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './budget.component.html',
  styleUrls: ['./budget.component.css']
})
export class BudgetComponent implements OnInit {

  budgets: any[] = [];
  selectedMonth = new Date().toISOString().slice(0, 7);
  showForm = false;

  // New budget form
  newCategory = '';
  newLimit: number | null = null;
  categories = ['FOOD', 'TRAVEL', 'SHOPPING', 'UTILITIES', 'ENTERTAINMENT', 'HEALTH', 'EDUCATION', 'RENT', 'TRANSPORT', 'INSURANCE', 'SUBSCRIPTIONS', 'GROCERIES', 'DINING', 'FITNESS', 'GIFTS', 'INVESTMENTS', 'OTHER'];

  constructor(private service: BudgetService) {}

  ngOnInit() {
    this.loadBudgets();
  }

  loadBudgets() {
    this.service.getBudgets(this.selectedMonth)
      .subscribe((res: any) => this.budgets = res || []);
  }

  createBudget() {
    if (!this.newCategory || !this.newLimit || this.newLimit <= 0) {
      alert('Please select a category and enter a valid limit');
      return;
    }

    const budget = {
      category: this.newCategory,
      limitAmount: this.newLimit,
      budgetMonth: this.selectedMonth + '-01'
    };

    this.service.createBudget(budget).subscribe({
      next: () => {
        this.loadBudgets();
        this.showForm = false;
        this.newCategory = '';
        this.newLimit = null;
      },
      error: (err: any) => {
        const msg = typeof err.error === 'string' ? err.error : (err.error?.message || 'Failed to create budget');
        alert(msg);
      }
    });
  }

  getProgressColor(pct: number): string {
    if (pct >= 100) return '#ef4444';
    if (pct >= 75) return '#f59e0b';
    return '#10b981';
  }

  getCategoryIcon(category: string): string {
    const icons: { [key: string]: string } = {
      'FOOD': '🍔', 'TRAVEL': '✈️', 'SHOPPING': '🛍️',
      'UTILITIES': '⚡', 'ENTERTAINMENT': '🎬', 'HEALTH': '🏥',
      'EDUCATION': '🎓', 'RENT': '🏠', 'TRANSPORT': '🚗',
      'INSURANCE': '🛡️', 'SUBSCRIPTIONS': '📺', 'GROCERIES': '🛒',
      'DINING': '🍽️', 'FITNESS': '💪', 'GIFTS': '🎁',
      'INVESTMENTS': '📈', 'OTHER': '📦'
    };
    return icons[category] || '📦';
  }

  getCardGlow(pct: number): string {
    if (pct >= 100) return 'rgba(239, 68, 68, 0.08)';
    if (pct >= 75) return 'rgba(245, 158, 11, 0.08)';
    return 'rgba(16, 185, 129, 0.08)';
  }

  getProgressGradient(pct: number): string {
    if (pct >= 100) return 'linear-gradient(90deg, #ef4444, #f87171)';
    if (pct >= 75) return 'linear-gradient(90deg, #f59e0b, #fbbf24)';
    return 'linear-gradient(90deg, #059669, #10b981)';
  }

  getCircleGradient(pct: number): string {
    const color = pct >= 100 ? '#ef4444' : pct >= 75 ? '#f59e0b' : '#10b981';
    const angle = Math.min(pct, 100) * 3.6;
    return `conic-gradient(${color} ${angle}deg, rgba(255,255,255,0.08) ${angle}deg)`;
  }

  getTotalLimit(): number {
    return this.budgets.reduce((sum, b) => sum + (b.limitAmount || 0), 0);
  }

  getTotalSpent(): number {
    return this.budgets.reduce((sum, b) => sum + (b.spentAmount || 0), 0);
  }

  getHealthScore(): number {
    const limit = this.getTotalLimit();
    return limit > 0 ? (this.getTotalSpent() / limit) * 100 : 0;
  }

  getSpendingShare(spent: number): number {
    const total = this.getTotalSpent();
    return total > 0 ? (spent / total) * 100 : 0;
  }

  getOverBudgetCount(): number {
    return this.budgets.filter(b => b.utilizationPercentage >= 100).length;
  }

  getSavings(): number {
    return Math.max(0, this.getTotalLimit() - this.getTotalSpent());
  }

  getTopCategory(): string {
    if (this.budgets.length === 0) return '';
    return this.budgets.reduce((max, b) => b.spentAmount > max.spentAmount ? b : max, this.budgets[0]).category;
  }
}