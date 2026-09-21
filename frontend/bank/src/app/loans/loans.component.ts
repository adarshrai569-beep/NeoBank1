import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LoanService } from './loan.service';
import { ApplyComponent } from './apply.component';
import { MyLoansComponent } from './my-loans.component';

@Component({
  standalone: true,
  selector: 'app-loans',
  imports: [CommonModule, ApplyComponent, MyLoansComponent],
  templateUrl: './loans.component.html',
  styleUrls: ['./loans.component.css']
})
export class LoansComponent implements OnInit {

  products: any[] = [];
  myApplications: any[] = [];
  showApply = false;
  showMyLoans = false;

  constructor(private loanService: LoanService, private router: Router) {}

  ngOnInit() {
    this.loadProducts();
    this.loadMyApplications();
  }

  loadProducts() {
    this.loanService.getProducts()
      .subscribe((res: any) => this.products = res || []);
  }

  loadMyApplications() {
    this.loanService.getMyApplications()
      .subscribe((res: any) => this.myApplications = res || []);
  }

  goToApply() {
    this.showApply = true;
    this.showMyLoans = false;
  }

  goToMyLoans() {
    this.showMyLoans = true;
    this.showApply = false;
  }

  goBackToProducts() {
    this.showApply = false;
    this.showMyLoans = false;
  }

  getApprovedCount(): number {
    return this.myApplications.filter(a => a.status === 'APPROVED').length;
  }

  getPendingCount(): number {
    return this.myApplications.filter(a => a.status === 'PENDING').length;
  }
}
