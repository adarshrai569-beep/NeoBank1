import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { LoanService } from './loan.service';

@Component({
  standalone: true,
  selector: 'app-repayment-schedule',
  imports: [CommonModule],
  templateUrl: './repayment-schedule.component.html'
})
export class RepaymentScheduleComponent implements OnInit, OnChanges {

  @Input() loanAccountId = 0;
  repayments: any[] = [];
  pageSize = 10;
  pageSizeOptions = [5, 10, 20, 50];
  currentPage = 1;
  payingIds = new Set<number>();

  constructor(private route: ActivatedRoute, private loanService: LoanService) {}

  ngOnInit() {
    // Read from route if not passed as @Input
    if (!this.loanAccountId) {
      this.loanAccountId = Number(this.route.snapshot.paramMap.get('loanAccountId') || 0);
    }
    if (this.loanAccountId) {
      this.loadSchedule();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['loanAccountId'] && this.loanAccountId) {
      this.loadSchedule();
    }
  }

  loadSchedule() {
    this.loanService.getRepayments(this.loanAccountId)
      .subscribe((res: any) => {
        this.repayments = res || [];
        this.currentPage = 1;
      });
  }

  markPaid(repaymentId: number) {
    if (this.payingIds.has(repaymentId)) return;
    this.payingIds.add(repaymentId);

    this.loanService.payRepayment(this.loanAccountId, repaymentId)
      .subscribe({
        next: () => {
          this.loadSchedule();
          alert('✅ Payment marked as paid');
        },
        error: () => alert('Failed to mark as paid'),
        complete: () => this.payingIds.delete(repaymentId)
      });
  }

  // Only allow paying the next pending EMI (no skipping)
  isNextPayable(repayment: any): boolean {
    const firstPending = this.repayments.find((r: any) => r.paymentStatus === 'PENDING');
    return firstPending && firstPending.id === repayment.id;
  }

  statusColor(status: string) {
    if (status === 'PAID') return '#4CAF50';
    if (status === 'OVERDUE') return '#F44336';
    return '#FF9800';
  }

  statusClass(status: string) {
    if (status === 'PAID') return 'badge badge-paid';
    if (status === 'OVERDUE') return 'badge badge-overdue';
    return 'badge badge-pending';
  }

  get totalPages() {
    return Math.max(1, Math.ceil(this.repayments.length / this.pageSize));
  }

  get startIndex() {
    return this.repayments.length === 0 ? 0 : ((this.currentPage - 1) * this.pageSize) + 1;
  }

  get endIndex() {
    if (this.repayments.length === 0) return 0;
    const end = this.currentPage * this.pageSize;
    return end > this.repayments.length ? this.repayments.length : end;
  }

  get pagedRepayments() {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.repayments.slice(start, start + this.pageSize);
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage += 1;
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage -= 1;
    }
  }

  onPageSizeChange(value: string) {
    const next = Number(value);
    if (!Number.isNaN(next) && next > 0) {
      this.pageSize = next;
      this.currentPage = 1;
    }
  }
}
