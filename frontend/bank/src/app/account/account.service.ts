import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  getAccounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/accounts`);
  }

  createAccount(accountType: string): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/accounts?type=${accountType}`,
      {}
    );
  }

  getAccount(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/accounts/${id}`);
  }

  transact(accountId: number, transactionData: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/accounts/${accountId}/transact`,
      transactionData
    );
  }

  getTransactions(accountId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/accounts/${accountId}/transactions`
    );
  }

  // ✅ ADD THIS: ACCOUNT → ACCOUNT TRANSFER
  transferMoney(payload: {
    fromAccountId: number;
    toAccountNumber: string;
    toCustomerId: string;
    amount: number;
    description?: string;
  }): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/accounts/transfer`,
      payload
    );
  }
}