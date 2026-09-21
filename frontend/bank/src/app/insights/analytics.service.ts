import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {

  private API = '/api';

  constructor(private http: HttpClient) {}

  // FR-8: User Analytics
  getSpending(userId: number, months: number = 6): Observable<any> {
    return this.http.get(`${this.API}/analytics/spending/${userId}?months=${months}`);
  }

  getWealth(userId: number): Observable<any> {
    return this.http.get(`${this.API}/analytics/wealth/${userId}`);
  }

  // FR-7: Admin Analytics
  getTransactionAnalytics(timeframe: string = '30d'): Observable<any> {
    return this.http.get(`${this.API}/admin/analytics/transactions?timeframe=${timeframe}`);
  }

  getLoanAnalytics(timeframe: string = '30d'): Observable<any> {
    return this.http.get(`${this.API}/admin/analytics/loans?timeframe=${timeframe}`);
  }

  getSystemLogs(page: number = 0, size: number = 20, status?: number): Observable<any> {
    let url = `${this.API}/admin/analytics/system-logs?page=${page}&size=${size}`;
    if (status) url += `&status=${status}`;
    return this.http.get(url);
  }
}
