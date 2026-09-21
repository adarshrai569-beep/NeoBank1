import { BudgetService } from '../user/budget.service';
import { BillService } from '../user/bill.service';
import { RewardService } from '../user/reward.service';
import { HttpClient } from '@angular/common/http';

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService } from '../account/account.service';
import { AuthService } from '../auth/auth.service';
import { UserService } from '../user/user.service';
import { BudgetComponent } from '../budget/budget.component';
import { BillsComponent } from '../bills/bills.component';
import { RewardsComponent } from '../rewards/reward.component';
import { LoansComponent } from '../loans/loans.component';
import { InsightsComponent } from '../insights/insights.component';
import { ApplyComponent } from '../loans/apply.component';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  imports: [CommonModule,FormsModule,BudgetComponent,BillsComponent,RewardsComponent,LoansComponent,InsightsComponent,ApplyComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  isAdmin = false;
  approved = false;
  showSplash = true;

menu:
  | 'ACCOUNT'
  | 'PROFILE'
  | 'DEPOSIT'
  | 'WITHDRAW'
  | 'TRANSFER'
  | 'HISTORY'
  | 'BUDGET'
  | 'BILLS'
  | 'REWARDS'
  | 'LOANS'
  | 'LOANS_APPLY'
  | 'INSIGHTS'
  | 'ADMIN_TRANSACTIONS'
  | 'ADMIN_LOGS'
  | 'ADMIN_USERS'
  | 'ADMIN_LOANS'
  | 'ADMIN_INSIGHTS' = 'ACCOUNT';



  currentComponent: any = null;
  userProfile: any = {};
  editableProfile: any = {};
  isEditingProfile = false;
  profilePhoto = '';

  defaultProfileImage =
    'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgdmlld0JveD0iMCAwIDEyMCAxMjAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMjAiIGhlaWdodD0iMTIwIiBmaWxsPSIjRjNGNEY2Ii8+CjxjaXJjbGUgY3g9IjYwIiBjeT0iNDAiIHI9IjIwIiBmaWxsPSIjOUI5QkEzIi8+CjxwYXRoIGQ9Ik0yMCA5MEMzMCA3MCA5MCA3MCA5MCA5MEgyMFoiIGZpbGw9IiM5QjlCQTMiLz4KPC9zdmc+';

  accounts: any[] = [];
  selectedAccount: any;
  transactions: any[] = [];

  // Toast notification
  showToast = false;
  toastTitle = '';
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  // Admin data
  allTransactions: any[] = [];
  systemLogs: any[] = [];
  adminData: any = {};
  adminUsers: any[] = [];
  adminRecentTransactions: any[] = [];
  pendingLoans: any[] = [];
  allLoans: any[] = [];
  bankInsights: any = {};

  // Deposit / Withdraw
  amount = 0;
  description = '';

  // Transfer
  transferAccountNumber = '';
  transferCustomerId = '';
  transferAmount = 0;
  transferDescription = '';

  // Stats
  totalBalance = 0;
  todayTransactions = 0;
  monthlySpending = 0;

  // Dashboard
  today = new Date();

  // History filters
  histFilter: 'ALL' | 'TODAY' | 'WEEK' | 'MONTH' | '3MONTHS' | 'CUSTOM' = 'ALL';
  histTypeFilter: 'ALL' | 'CREDIT' | 'DEBIT' = 'ALL';
  histDateFrom = '';
  histDateTo = '';
  todayStr = new Date().toISOString().split('T')[0];

  // Mobile navigation state
  isMobileMenuOpen = false;
// bills rewards budget
budgets: any[] = [];
bills: any[] = [];
rewardPoints = 0;

selectedMonth = new Date().toISOString().slice(0, 7);
constructor(
  private accService: AccountService,
  private authService: AuthService,
  private userService: UserService,
  private budgetService: BudgetService,
  private billService: BillService,
  private rewardService: RewardService,
  private router: Router,
  private http: HttpClient
) {}

  ngOnInit() {
    // Show splash for 2 seconds on initial load
    setTimeout(() => {
      this.showSplash = false;
    }, 2000);

    this.isAdmin = this.authService.getRole() === 'ROLE_ADMIN';
    this.loadUserProfile();
    if (this.isAdmin) {
      this.loadAdminDashboard();
    }
  }

  loadAdminDashboard() {
    this.http.get<any>('/api/admin/dashboard').subscribe({
      next: (res) => this.adminData = res || {},
      error: () => {}
    });
    this.http.get<any[]>('/api/admin/users').subscribe({
      next: (res) => this.adminUsers = res || [],
      error: () => {}
    });
    this.http.get<any[]>('/api/admin/all-transactions').subscribe({
      next: (res) => this.adminRecentTransactions = (res || []).slice(0, 10),
      error: () => {}
    });
  }

  loadUserProfile() {
    this.userService.getProfile().subscribe({
      next: (profile) => {
        console.log('✅ User profile loaded:', profile); // Debug log
        this.userProfile = profile;
        this.editableProfile = { ...profile };
        
        // Debug: Check what properties are available
        console.log('Profile properties:', Object.keys(profile));
        console.log('Customer ID field:', profile.id || profile.customerId || profile.userId);
        
        this.profilePhoto = profile.profilePhoto;
        this.isAdmin = profile.role === 'ADMIN';
        this.approved = profile.approved;
        
        if (this.approved) {
          this.loadAccounts();
          this.loadRewards();
        }
      },
      error: (err) => {
        console.error('❌ Failed to load user profile:', err);
        if (err.status === 401) {
          this.logout();
        }
      }
    });
  }

  loadAccounts() {
    this.accService.getAccounts().subscribe({
      next: acc => {
        this.accounts = acc || [];
        this.selectedAccount =
          this.accounts.find(a => a.isActive && !a.isFrozen) || null;

        if (this.selectedAccount) {
          this.loadTransactions();
        }
        this.calculateStats();
      },
      error: () => this.logout()
    });
  }

  loadTransactions() {
    if (!this.selectedAccount) return;

    this.accService.getTransactions(this.selectedAccount.id).subscribe({
      next: tx => {
        this.transactions = tx || [];
        this.calculateStats();
      },
      error: err => {
        if (err.status === 401) this.logout();
        this.transactions = [];
      }
    });
  }

  calculateStats() {
    this.totalBalance = this.accounts.reduce(
      (sum, acc) => sum + (acc.balance || 0),
      0
    );

    const today = new Date().toDateString();
    this.todayTransactions = this.transactions.filter(
      tx => new Date(tx.transactionDate).toDateString() === today
    ).length;

    const m = new Date().getMonth();
    const y = new Date().getFullYear();

    this.monthlySpending = this.transactions
      .filter(tx => {
        const d = new Date(tx.transactionDate);
        return d.getMonth() === m && d.getFullYear() === y && tx.type === 'DEBIT';
      })
      .reduce((s, tx) => s + tx.amount, 0);
  }

  // ---------- History Stats ----------
  getTotalCredit(): number {
    return this.transactions.filter(tx => tx.type === 'CREDIT').reduce((s, tx) => s + tx.amount, 0);
  }

  getTotalDebit(): number {
    return this.transactions.filter(tx => tx.type === 'DEBIT').reduce((s, tx) => s + tx.amount, 0);
  }

  getMonthlyIncome(): number {
    const m = new Date().getMonth();
    const y = new Date().getFullYear();
    return this.transactions
      .filter(tx => {
        const d = new Date(tx.transactionDate);
        return d.getMonth() === m && d.getFullYear() === y && tx.type === 'CREDIT';
      })
      .reduce((s, tx) => s + tx.amount, 0);
  }

  getCreditCount(): number {
    return this.transactions.filter(tx => tx.type === 'CREDIT').length;
  }

  getDebitCount(): number {
    return this.transactions.filter(tx => tx.type === 'DEBIT').length;
  }

  getCreditPercentage(): number {
    const total = this.getTotalCredit() + this.getTotalDebit();
    return total > 0 ? (this.getTotalCredit() / total) * 100 : 50;
  }

  getDebitPercentage(): number {
    return 100 - this.getCreditPercentage();
  }

  getAvgTransaction(): number {
    return this.transactions.length > 0
      ? this.transactions.reduce((s, tx) => s + tx.amount, 0) / this.transactions.length
      : 0;
  }

  getLargestTransaction(): number {
    return this.transactions.length > 0
      ? Math.max(...this.transactions.map(tx => tx.amount))
      : 0;
  }

  // ---------- History Filters ----------
  setHistFilter(filter: 'ALL' | 'TODAY' | 'WEEK' | 'MONTH' | '3MONTHS' | 'CUSTOM') {
    this.histFilter = filter;
    if (filter !== 'CUSTOM') {
      this.histDateFrom = '';
      this.histDateTo = '';
    }
  }

  clearCustomDates() {
    this.histDateFrom = '';
    this.histDateTo = '';
    this.histFilter = 'ALL';
  }

  getFilteredTransactions(): any[] {
    let filtered = this.transactions;

    // Date filter
    const now = new Date();
    if (this.histFilter === 'TODAY') {
      const today = now.toDateString();
      filtered = filtered.filter(tx => new Date(tx.transactionDate).toDateString() === today);
    } else if (this.histFilter === 'WEEK') {
      const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      filtered = filtered.filter(tx => new Date(tx.transactionDate) >= weekAgo);
    } else if (this.histFilter === 'MONTH') {
      const m = now.getMonth(), y = now.getFullYear();
      filtered = filtered.filter(tx => {
        const d = new Date(tx.transactionDate);
        return d.getMonth() === m && d.getFullYear() === y;
      });
    } else if (this.histFilter === '3MONTHS') {
      const threeMonthsAgo = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000);
      filtered = filtered.filter(tx => new Date(tx.transactionDate) >= threeMonthsAgo);
    } else if (this.histFilter === 'CUSTOM') {
      if (this.histDateFrom) {
        const from = new Date(this.histDateFrom);
        from.setHours(0, 0, 0, 0);
        filtered = filtered.filter(tx => new Date(tx.transactionDate) >= from);
      }
      if (this.histDateTo) {
        const to = new Date(this.histDateTo);
        to.setHours(23, 59, 59, 999);
        filtered = filtered.filter(tx => new Date(tx.transactionDate) <= to);
      }
    }

    // Type filter
    if (this.histTypeFilter !== 'ALL') {
      filtered = filtered.filter(tx => tx.type === this.histTypeFilter);
    }

    return filtered;
  }

  getFilteredCredit(): number {
    return this.getFilteredTransactions().filter(tx => tx.type === 'CREDIT').reduce((s, tx) => s + tx.amount, 0);
  }

  getFilteredDebit(): number {
    return this.getFilteredTransactions().filter(tx => tx.type === 'DEBIT').reduce((s, tx) => s + tx.amount, 0);
  }

  // ---------- Deposit & Withdraw ----------
  deposit() {
    this.processTxn('DEPOSIT');
  }

  withdraw() {
    this.processTxn('WITHDRAW');
  }

  processTxn(type: 'DEPOSIT' | 'WITHDRAW') {
    if (!this.selectedAccount || this.amount <= 0) return;

    this.accService.transact(this.selectedAccount.id, {
      type,
      amount: this.amount,
      description: this.description || `${type} transaction`
    }).subscribe({
      next: () => {
        this.showNotification('Success', 'Transaction completed successfully!', 'success');
        this.amount = 0;
        this.description = '';
        this.loadAccounts();
      },
      error: err => this.showNotification('Error', err.error?.error || 'Transaction failed', 'error')
    });
  }

  // ---------- Account → Account Transfer ----------
  transferMoney() {
    if (!this.selectedAccount || this.transferAmount <= 0) return;

    this.accService.transferMoney({
      fromAccountId: this.selectedAccount.id,
      toAccountNumber: this.transferAccountNumber,
      toCustomerId: this.transferCustomerId,
      amount: this.transferAmount,
      description: this.transferDescription || 'Account transfer'
    }).subscribe({
      next: () => {
        this.showNotification('Success', 'Transfer completed successfully!', 'success');
        this.transferAccountNumber = '';
        this.transferCustomerId = '';
        this.transferAmount = 0;
        this.transferDescription = '';
        this.menu = 'ACCOUNT';
        this.loadAccounts();
      },
      error: err => this.showNotification('Error', err.error?.error || 'Transfer failed', 'error')
    });
  }

  // ---------- Profile ----------
  startEditProfile() {
    this.isEditingProfile = true;
    this.editableProfile = { ...this.userProfile };
  }

  cancelEditProfile() {
    this.isEditingProfile = false;
  }

  saveProfile() {
    this.userService.updateProfile({
      fullName: this.editableProfile.fullName,
      mobile: this.editableProfile.mobile,
      address: this.editableProfile.address
    }).subscribe({
      next: u => {
        this.userProfile = u;
        this.isEditingProfile = false;
        this.showNotification('Success', 'Profile updated successfully!', 'success');
      }
    });
  }

  onPhotoSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      this.showNotification('Error', 'Image must be less than 2MB', 'error');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const base64 = reader.result as string;
      sessionStorage.setItem('profilePhoto', base64);
      this.profilePhoto = base64;
    };
    reader.readAsDataURL(file);
  }

  getProfileImage(): string {
    const saved = sessionStorage.getItem('profilePhoto');
    if (saved) return saved;
    return this.defaultProfileImage;
  }

  // ---------- Toast Notification ----------
  showNotification(title: string, message: string, type: 'success' | 'error' = 'success') {
    this.toastTitle = title;
    this.toastMessage = message;
    this.toastType = type;
    this.showToast = true;
  }

  closeToast() {
    this.showToast = false;
  }

  // ---------- Utils ----------
  formatCurrency(amount: number): string {
    return `₹${amount.toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })}`;
  }

  canPerformTransactions(): boolean {
    return this.selectedAccount?.isActive && !this.selectedAccount?.isFrozen;
  }

  goToAdmin() {
    this.router.navigate(['/admin']);
  }

  goToLoans() {
    this.router.navigate(['/loans']);
  }

  goToInsights() {
    this.router.navigate(['/insights']);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  //bills budget reward
  // ✅ Budget

loadBudgets() {
  this.budgetService.getBudgets(this.selectedMonth)
    .subscribe((res: any) => this.budgets = res || []);
}


// ✅ Bills
loadBills() {
  this.billService.getBills()
    .subscribe((res: any) => this.bills = res || []);
}

// markBillPaid(id: number) {
//   this.billService.updateStatus(id, 'PAID').subscribe(() => {
//     alert('✅ Bill Paid');
//     this.loadBills();
//     this.loadRewards();
//   });
// }

// ✅ Rewards

loadRewards() {
  if (!this.userProfile?.id) return;

  this.rewardService.getRewards(this.userProfile.id)
    .subscribe({
      next: (res: any) => {
        this.rewardPoints = res?.balance ?? res?.pointsBalance ?? 0; 
      },
      error: () => {
        this.rewardPoints = 0; 
      }
    });
}



  // Add helper method to get customer ID safely
  
getCustomerId(): string {
    return this.userProfile?.customerId || 'Not Available';
  }

  // Mobile navigation methods
  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
  
  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }
  
 
setMenu(menu: any): void {
  this.menu = menu;
  this.closeMobileMenu();

  if (menu === 'BUDGET') {
    this.currentComponent = BudgetComponent;
    this.loadBudgets(); 
  }
  else if (menu === 'BILLS') {
    this.currentComponent = BillsComponent;
    this.loadBills(); 
  }
  else if (menu === 'REWARDS') {
    this.currentComponent = RewardsComponent;
    this.loadRewards(); 
  }
  else if (menu === 'ADMIN_TRANSACTIONS') {
    this.loadAllTransactions();
  }
  else if (menu === 'ADMIN_LOGS') {
    this.loadSystemLogs();
  }
  else if (menu === 'ADMIN_USERS') {
    this.loadAdminUsers();
  }
  else if (menu === 'ADMIN_LOANS') {
    this.loadAdminLoans();
  }
  else if (menu === 'ADMIN_INSIGHTS') {
    this.loadBankInsights();
  }
  else {
    this.currentComponent = null;
  }
}

loadAllTransactions() {
  this.http.get<any[]>('/api/admin/all-transactions')
    .subscribe({
      next: (res) => this.allTransactions = res || [],
      error: () => this.allTransactions = []
    });
}

loadSystemLogs() {
  this.http.get<any>('/api/admin/analytics/system-logs')
    .subscribe({
      next: (res) => this.systemLogs = res?.logs || res || [],
      error: () => this.systemLogs = []
    });
}

loadAdminUsers() {
  this.http.get<any[]>('/api/admin/users')
    .subscribe({
      next: (res) => this.adminUsers = res || [],
      error: () => this.adminUsers = []
    });
}

loadAdminLoans() {
  this.http.get<any[]>('/api/loans/admin/applications')
    .subscribe({
      next: (res) => {
        this.allLoans = res || [];
        this.pendingLoans = this.allLoans.filter((l: any) => l.status === 'PENDING');
      },
      error: () => { this.allLoans = []; this.pendingLoans = []; }
    });
}

toggleUserStatus(user: any) {
  const newStatus = !user.isActive;
  this.http.patch(`/api/admin/users/${user.id}/status`, { isActive: newStatus })
    .subscribe({
      next: () => {
        this.showNotification('Success', `User ${user.fullName} ${newStatus ? 'activated' : 'deactivated'}`, 'success');
        this.loadAdminUsers();
        this.loadAdminDashboard();
      },
      error: (err) => this.showNotification('Error', err.error?.message || 'Failed to update status', 'error')
    });
}

approveLoan(loanId: number) {
  this.http.put(`/api/loans/${loanId}/decision`, { decision: 'APPROVED', remarks: 'Approved by admin' })
    .subscribe({
      next: () => {
        this.showNotification('Success', 'Loan approved successfully!', 'success');
        this.loadAdminLoans();
        this.loadAdminDashboard();
      },
      error: (err) => this.showNotification('Error', err.error?.message || 'Failed to approve', 'error')
    });
}

rejectLoan(loanId: number) {
  this.http.put(`/api/loans/${loanId}/decision`, { decision: 'REJECTED', remarks: 'Rejected by admin' })
    .subscribe({
      next: () => {
        this.showNotification('Success', 'Loan rejected', 'success');
        this.loadAdminLoans();
        this.loadAdminDashboard();
      },
      error: (err) => this.showNotification('Error', err.error?.message || 'Failed to reject', 'error')
    });
}

loadBankInsights() {
  this.http.get<any>('/api/admin/analytics/transactions?timeframe=30d')
    .subscribe({
      next: (res) => this.bankInsights = res || {},
      error: () => this.bankInsights = {}
    });
}

getBarHeight(value: number, maxVal: number): number {
  if (!maxVal || !value) return 4;
  return Math.max(4, Math.min(100, (value / maxVal) * 100));
}

getMaxVolume(): number {
  if (!this.bankInsights?.dailyVolumes) return 1;
  return Math.max(...this.bankInsights.dailyVolumes.map((d: any) => d[2] || 0), 1);
}

getFlowPercent(value: number, inflow: number, outflow: number): number {
  const total = (inflow || 0) + (outflow || 0);
  if (!total) return 0;
  return Math.round(((value || 0) / total) * 100);
}

getNetPercent(): number {
  const inflow = this.bankInsights?.totalInflow || 0;
  const outflow = this.bankInsights?.totalOutflow || 0;
  if (!inflow) return 0;
  return Math.round(((inflow - outflow) / inflow) * 100);
}


  
  // Enhanced transfer validation
  canPerformTransfer(): boolean {
    return !!(this.transferAccountNumber && 
             this.transferCustomerId && 
             this.transferAmount && 
             this.transferAmount > 0 &&
             this.selectedAccount &&
             this.transferAmount <= this.selectedAccount.balance);
  }



}



// import { BudgetService } from '../user/budget.service';
// import { BillService } from '../user/bill.service';
// import { RewardService } from '../user/reward.service';

// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { Router } from '@angular/router';
// import { AccountService } from '../account/account.service';
// import { AuthService } from '../auth/auth.service';
// import { UserService } from '../user/user.service';
// import { BudgetComponent } from '../budget/budget.component';
// import { BillsComponent } from '../bills/bills.component';
// import { RewardsComponent } from '../rewards/reward.component';

// // ✅ ✅ ADD THIS IMPORT
// import { LoanService } from '../loans/loan.service';

// @Component({
//   standalone: true,
//   selector: 'app-dashboard',
//   imports: [CommonModule, FormsModule, BudgetComponent, BillsComponent, RewardsComponent],
//   templateUrl: './dashboard.component.html',
//   styleUrls: ['./dashboard.component.css']
// })
// export class DashboardComponent implements OnInit {

//   isAdmin = false;
//   approved = false;

//   menu:
//     | 'ACCOUNT'
//     | 'PROFILE'
//     | 'DEPOSIT'
//     | 'WITHDRAW'
//     | 'TRANSFER'
//     | 'HISTORY'
//     | 'BUDGET'
//     | 'BILLS'
//     | 'REWARDS'
//     | 'LOAN_APPLY'   // ✅ NEW
//     | 'MY_LOANS'     // ✅ NEW
//     = 'ACCOUNT';

//   currentComponent: any = null;
//   userProfile: any = {};
//   editableProfile: any = {};
//   isEditingProfile = false;
//   profilePhoto = '';

//   defaultProfileImage = 'data:image/svg+xml;base64,...';

//   accounts: any[] = [];
//   selectedAccount: any;
//   transactions: any[] = [];

//   amount = 0;
//   description = '';

//   transferAccountNumber = '';
//   transferCustomerId = '';
//   transferAmount = 0;
//   transferDescription = '';

//   totalBalance = 0;
//   todayTransactions = 0;
//   monthlySpending = 0;

//   isMobileMenuOpen = false;

//   budgets: any[] = [];
//   bills: any[] = [];
//   rewardPoints = 0;

//   selectedMonth = new Date().toISOString().slice(0, 7);

//   // ✅ ✅ LOAN VARIABLES
//   loanProducts: any[] = [];
//   selectedProduct: any;
//   loanAmount: number = 0;
//   loanTenure: number = 0;
//   myLoans: any[] = [];

//   constructor(
//     private accService: AccountService,
//     private authService: AuthService,
//     private userService: UserService,
//     private budgetService: BudgetService,
//     private billService: BillService,
//     private rewardService: RewardService,
//     private loanService: LoanService, // ✅ NEW
//     private router: Router
//   ) {}

//   ngOnInit() {
//     this.isAdmin = this.authService.getRole() === 'ROLE_ADMIN';
//     this.loadUserProfile();
//   }

//   loadUserProfile() {
//     this.userService.getProfile().subscribe({
//       next: (profile) => {
//         this.userProfile = profile;
//         this.editableProfile = { ...profile };
//         this.profilePhoto = profile.profilePhoto;
//         this.isAdmin = profile.role === 'ADMIN';
//         this.approved = profile.approved;

//         if (this.approved) {
//           this.loadAccounts();
//         }
//       },
//       error: () => this.logout()
//     });
//   }

//   loadAccounts() {
//     this.accService.getAccounts().subscribe({
//       next: acc => {
//         this.accounts = acc || [];
//         this.selectedAccount =
//           this.accounts.find(a => a.isActive && !a.isFrozen) || null;

//         if (this.selectedAccount) {
//           this.loadTransactions();
//         }
//         this.calculateStats();
//       },
//       error: () => this.logout()
//     });
//   }

//   loadTransactions() {
//     if (!this.selectedAccount) return;

//     this.accService.getTransactions(this.selectedAccount.id).subscribe({
//       next: tx => {
//         this.transactions = tx || [];
//         this.calculateStats();
//       }
//     });
//   }

//   calculateStats() {
//     this.totalBalance = this.accounts.reduce(
//       (sum, acc) => sum + (acc.balance || 0),
//       0
//     );

//     const today = new Date().toDateString();
//     this.todayTransactions = this.transactions.filter(
//       tx => new Date(tx.transactionDate).toDateString() === today
//     ).length;

//     const m = new Date().getMonth();
//     const y = new Date().getFullYear();

//     this.monthlySpending = this.transactions
//       .filter(tx => {
//         const d = new Date(tx.transactionDate);
//         return d.getMonth() === m && d.getFullYear() === y && tx.type === 'DEBIT';
//       })
//       .reduce((s, tx) => s + tx.amount, 0);
//   }

//   // ✅ TRANSACTIONS
//   deposit() { this.processTxn('DEPOSIT'); }
//   withdraw() { this.processTxn('WITHDRAW'); }

//   processTxn(type: any) {
//     if (!this.selectedAccount || this.amount <= 0) return;

//     this.accService.transact(this.selectedAccount.id, {
//       type,
//       amount: this.amount,
//       description: this.description || ''
//     }).subscribe(() => {
//       alert('✅ Success');
//       this.loadAccounts();
//     });
//   }

//   transferMoney() {
//     if (!this.selectedAccount || this.transferAmount <= 0) return;

//     this.accService.transferMoney({
//       fromAccountId: this.selectedAccount.id,
//       toAccountNumber: this.transferAccountNumber,
//       toCustomerId: this.transferCustomerId,
//       amount: this.transferAmount,
//       description: this.transferDescription
//     }).subscribe(() => {
//       alert('✅ Transfer Done');
//       this.loadAccounts();
//     });
//   }

//   // ✅ ✅ LOAN METHODS

//   loadLoanProducts() {
//     this.loanService.getProducts().subscribe((res: any) => {
//       this.loanProducts = res;
//     });
//   }

//   applyLoan() {

//     const request = {
//       productId: this.selectedProduct.id,
//       amount: this.loanAmount,
//       tenure: this.loanTenure
//     };

//     this.loanService.applyLoan(request).subscribe({
//       next: () => {
//         alert('✅ Loan Applied');
//         this.loanAmount = 0;
//         this.loadMyLoans();
//       },
// error: (err: any) => alert(err.error)

//     });
//   }

//   loadMyLoans() {
//     this.loanService.getMyApplications().subscribe((res: any) => {
//       this.myLoans = res;
//     });
//   }

//   viewEMI(id: number) {
//     this.loanService.getEMI(id).subscribe((res: any) => {
//       alert(
//         "EMI: ₹" + res.monthlyEMI +
//         "\nInterest: ₹" + res.totalInterest +
//         "\nTotal: ₹" + res.totalAmount
//       );
//     });
//   }

//   // ✅ UI
//   setMenu(menu: any): void {
//     this.menu = menu;
//     this.closeMobileMenu();

//     if (menu === 'BUDGET') this.loadBudgets();
//     if (menu === 'BILLS') this.loadBills();
//     if (menu === 'REWARDS') this.loadRewards();

//     // ✅ LOAN TRIGGERS
//     if (menu === 'LOAN_APPLY') this.loadLoanProducts();
//     if (menu === 'MY_LOANS') this.loadMyLoans();
//   }

//   formatCurrency(amount: number): string {
//     return `₹${amount}`;
//   }

//   logout() {
//     this.authService.logout();
//     this.router.navigate(['/login']);
//   }

//   loadBudgets() {
//     this.budgetService.getBudgets(this.selectedMonth)
//       .subscribe((res: any) => this.budgets = res);
//   }

//   loadBills() {
//     this.billService.getBills()
//       .subscribe((res: any) => this.bills = res);
//   }

//   loadRewards() {
//     this.rewardService.getRewards(this.userProfile.id)
//       .subscribe((res: any) => this.rewardPoints = res.pointsBalance);
//   }

//   toggleMobileMenu() {
//     this.isMobileMenuOpen = !this.isMobileMenuOpen;
//   }

//   closeMobileMenu() {
//     this.isMobileMenuOpen = false;
//   }

//   canPerformTransfer(): boolean {
//     return !!this.transferAccountNumber &&
//            !!this.transferCustomerId &&
//            this.transferAmount > 0;
//   }
// }