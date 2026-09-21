import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class BudgetService {

  private baseUrl = '/api/budgets';

  constructor(private http: HttpClient) {}

  createBudget(data: any) {
    return this.http.post(this.baseUrl, data);
  }

  // ✅ FIXED (removed userId)
  getBudgets(month: string) {
    return this.http.get(`${this.baseUrl}/${month}`);
  }
}