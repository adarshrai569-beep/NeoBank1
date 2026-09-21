import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoanService {

  private baseUrl = '/api/loans';

  constructor(private http: HttpClient) {}

  getProducts() {
    return this.http.get(`${this.baseUrl}/products`);
  }

  // Admin: create loan product
  createProduct(data: any) {
    return this.http.post(`${this.baseUrl}/products`, data);
  }

  applyLoan(data: any) {
    return this.http.post(`${this.baseUrl}/apply`, data);
  }

  getMyApplications() {
    return this.http.get(`${this.baseUrl}/my-applications`);
  }

  getEMI(id: number) {
    return this.http.get(`${this.baseUrl}/${id}/emi`);
  }

  // Admin: get all loan applications
  getAllApplications() {
    return this.http.get(`${this.baseUrl}/admin/applications`);
  }

  // Admin: approve or reject application
  decideLoan(id: number, decision: string, remarks?: string) {
    return this.http.put(`${this.baseUrl}/${id}/decision`, { decision, remarks });
  }

  getMyAccounts() {
    return this.http.get(`${this.baseUrl}/my-accounts`);
  }

  getRepayments(loanAccountId: number, status?: string) {
    const query = status ? `?status=${encodeURIComponent(status)}` : '';
    return this.http.get(`${this.baseUrl}/${loanAccountId}/repayments${query}`);
  }

  payRepayment(loanAccountId: number, repaymentId: number) {
    return this.http.patch(`${this.baseUrl}/${loanAccountId}/repayments/${repaymentId}/pay`, {});
  }
}
