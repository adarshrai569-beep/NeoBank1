import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from './admin.service';
import { LoanService } from '../loans/loan.service';

@Component({
  standalone: true,
  selector: 'app-admin',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  activeTab: 'dashboard' | 'pending' | 'users' | 'health' = 'dashboard';

  dashboard: any = null;
  pendingApprovals: any[] = [];
  users: any[] = [];
  systemHealth: any = null;
  activityUser: any = null;
  userActivity: any = null;
  activityLoading = false;

  pendingModule = 'ALL';

  constructor(
    private adminService: AdminService,
    private loanService: LoanService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadDashboard();
  }

  setTab(tab: 'dashboard' | 'pending' | 'users' | 'health') {
    this.activeTab = tab;

    if (tab === 'dashboard') {
      this.loadDashboard();
    }
    if (tab === 'pending') {
      this.loadPendingApprovals();
    }
    if (tab === 'users') {
      this.loadUsers();
    }
    if (tab === 'health') {
      this.loadSystemHealth();
    }
  }

  loadDashboard() {
    this.adminService.getDashboard().subscribe({
      next: (data) => this.dashboard = data,
      error: () => this.dashboard = null
    });
  }

  loadPendingApprovals() {
    const moduleParam = this.pendingModule === 'LOAN' ? 'LOAN' : undefined;
    this.adminService.getPendingApprovals(moduleParam).subscribe({
      next: (data) => this.pendingApprovals = data || [],
      error: () => this.pendingApprovals = []
    });
  }

  decidePendingLoan(id: number, decision: 'APPROVED' | 'REJECTED', remarks?: string) {
    this.loanService.decideLoan(id, decision, remarks)
      .subscribe(() => this.loadPendingApprovals());
  }

  loadUsers() {
    this.adminService.getAllUsers().subscribe({
      next: (data) => this.users = data || [],
      error: () => this.users = []
    });
  }

  loadSystemHealth() {
    this.adminService.getSystemHealth().subscribe({
      next: (data) => this.systemHealth = data,
      error: () => this.systemHealth = null
    });
  }

  updateUserStatus(user: any, isActive: boolean) {
    this.adminService.updateUserStatus(user.id, { isActive })
      .subscribe(() => this.loadUsers());
  }

  loadUserActivity(user: any) {
    this.activityUser = user;
    this.userActivity = null;
    this.activityLoading = true;

    this.adminService.getUserActivity(user.id).subscribe({
      next: (data) => {
        this.userActivity = data;
        this.activityLoading = false;
      },
      error: () => {
        this.userActivity = null;
        this.activityLoading = false;
      }
    });
  }

  clearUserActivity() {
    this.activityUser = null;
    this.userActivity = null;
  }

  formatCurrency(value: number | null | undefined): string {
    const amount = value ?? 0;
    return `₹${amount.toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })}`;
  }

  formatDate(value: string | null | undefined): string {
    if (!value) return '-';
    return new Date(value).toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  formatDateTime(value: string | null | undefined): string {
    if (!value) return '-';
    return new Date(value).toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }
}
