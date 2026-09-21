import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ForgotPasswordComponent } from './auth/forgot-password/forgot-password.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from './auth/auth.guard';
import { AdminAuthGuard } from './auth/admin-auth.guard';
import { AdminComponent } from './admin/admin.cmponent';
import { LoansComponent } from './loans/loans.component';
import { MyLoansComponent } from './loans/my-loans.component';
import { RepaymentScheduleComponent } from './loans/repayment-schedule.component';
// import { ApplyComponent } from './loans/apply.component';
import { LoanProductsComponent } from './admin/loan-products.component';
import { ApplyComponent } from './loans/apply.component';
import { InsightsComponent } from './insights/insights.component';
import { BillsComponent } from './bills/bills.component';
import { BudgetComponent } from './budget/budget.component';
import { RewardsComponent } from './rewards/reward.component';
import { PaymentHomeComponent } from './bills/payment-home.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'loans',
    component: LoansComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'loans/my-loans',
    component: MyLoansComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'loans/repayment-schedule/:loanAccountId',
    component: RepaymentScheduleComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'loans/apply',
    component: ApplyComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'insights',
    component: InsightsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [AdminAuthGuard]
  },
  {
    path: 'admin/loan-products',
    component: LoanProductsComponent,
    canActivate: [AdminAuthGuard]
  },
  {
    path: 'bills',
    component: BillsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'payments',
    component: PaymentHomeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'budget',
    component: BudgetComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'rewards',
    component: RewardsComponent,
    canActivate: [AuthGuard]
  }
];
