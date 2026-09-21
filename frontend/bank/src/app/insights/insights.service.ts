import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FinancialInsights } from './insights.model';

@Injectable({ providedIn: 'root' })
export class InsightsService {

  private API = '/api/insights';

  constructor(private http: HttpClient) {}

  getInsights(userId: number) {
    return this.http.get<FinancialInsights>(`${this.API}/${userId}`);
  }
}
