import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LoanService } from './loan.service';
import { RepaymentScheduleComponent } from './repayment-schedule.component';

@Component({
  standalone: true,
  selector: 'app-my-loans',
  imports: [CommonModule, RepaymentScheduleComponent],
  templateUrl: './my-loans.component.html',
  styleUrls: ['./my-loans.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class MyLoansComponent implements OnInit {

  accounts: any[] = [];
  selectedAccountId: number | null = null;

  constructor(private loanService: LoanService, private router: Router) {}

  ngOnInit() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.loanService.getMyAccounts()
      .subscribe((res: any) => this.accounts = res || []);
  }

  openSchedule(accountId: number) {
    this.selectedAccountId = accountId;
  }

  backToAccounts() {
    this.selectedAccountId = null;
  }
}
