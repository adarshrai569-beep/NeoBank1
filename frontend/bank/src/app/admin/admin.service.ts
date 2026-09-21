import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AdminService {

  private API = '/api/admin';

  constructor(private http: HttpClient) {}

  // ✅ DASHBOARD
  getDashboard() {
    return this.http.get<any>(`${this.API}/dashboard`);
  }

  // ✅ PENDING APPROVALS
  getPendingApprovals(module?: string) {
    const url = module ? `${this.API}/pending-approvals?module=${module}` : `${this.API}/pending-approvals`;
    return this.http.get<any[]>(url);
  }

  // ✅ SYSTEM HEALTH
  getSystemHealth() {
    return this.http.get<any>(`${this.API}/system-health`);
  }

  // ✅ USERS
  getAllUsers() {
    return this.http.get<any[]>(`${this.API}/users`);
  }

  updateUserStatus(id: number, payload: { isActive: boolean }) {
    return this.http.patch(`${this.API}/users/${id}/status`, payload);
  }

  getUserActivity(id: number) {
    return this.http.get<any>(`${this.API}/users/${id}/activity`);
  }

  updateRole(id: number, role: string) {
    return this.http.put(`${this.API}/users/${id}/role?role=${role}`, {});
  }

  approveUser(id: number) {
    return this.http.put(`${this.API}/users/${id}/approve`, {});
  }

  deleteUser(id: number) {
    return this.http.delete(`${this.API}/users/${id}`);
  }

  // ✅ ACCOUNT FREEZE (CORRECT BANKING LOGIC)
  freezeAccount(accountId: number) {
    return this.http.put(
      `${this.API}/accounts/${accountId}/freeze`,
      {}
    );
  }

  unfreezeAccount(accountId: number) {
    return this.http.put(
      `${this.API}/accounts/${accountId}/unfreeze`,
      {}
    );
  }
}