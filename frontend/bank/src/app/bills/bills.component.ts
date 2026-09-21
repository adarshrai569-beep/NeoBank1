import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BillService } from '../user/bill.service';

@Component({
  selector: 'app-bills',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bills.component.html',
  styleUrls: ['./bills.component.css']
})
export class BillsComponent implements OnInit {

  bills: any[] = [];
  selectedPayment: { [key: number]: string } = {};
  showForm = false;
  newBiller = '';
  customBiller = '';
  newAmount: number | null = null;
  newDueDate = '';

  // Categories matching budget categories
  billerCategories = ['SHOPPING', 'FOOD', 'TRAVEL', 'UTILITIES', 'ENTERTAINMENT', 'HEALTH', 'EDUCATION', 'RENT', 'TRANSPORT', 'INSURANCE', 'SUBSCRIPTIONS', 'GROCERIES', 'DINING', 'FITNESS', 'GIFTS', 'INVESTMENTS', 'OTHER'];

  constructor(private billService: BillService) {}

  ngOnInit(): void {
    this.loadBills();
  }

  loadBills(): void {
    this.billService.getBills().subscribe({
      next: (res: any) => {
        this.bills = res || [];
      },
      error: () => {
        this.bills = [];
      }
    });
  }

  createBill(): void {
    const billerName = this.newBiller === 'OTHER' ? this.customBiller : this.newBiller;

    if (!billerName || !this.newAmount || !this.newDueDate) {
      alert('Please fill all fields');
      return;
    }

    const bill = {
      billerName: billerName.toLowerCase(),
      amount: this.newAmount,
      dueDate: this.newDueDate
    };

    this.billService.createBill(bill).subscribe({
      next: () => {
        this.loadBills();
        this.showForm = false;
        this.newBiller = '';
        this.customBiller = '';
        this.newAmount = null;
        this.newDueDate = '';
      },
      error: (err: any) => {
        const msg = typeof err.error === 'string' ? err.error : 'Failed to create bill';
        alert(msg);
      }
    });
  }

  pay(id: number): void {
    const method = this.selectedPayment[id];
    if (!method) {
      alert('Select payment method');
      return;
    }

    this.billService.payBill(id, method).subscribe(() => {
      alert('Payment Successful! +10 Reward Points');
      this.loadBills();
    });
  }

  getPaidCount(): number {
    return this.bills.filter(b => b.status === 'PAID').length;
  }

  getPendingCount(): number {
    return this.bills.filter(b => b.status !== 'PAID').length;
  }

  getTotalAmount(): number {
    return this.bills.reduce((sum, b) => sum + (b.amount || 0), 0);
  }

  getBillerIcon(name: string): string {
    const n = (name || '').toLowerCase();
    if (n.includes('electric') || n.includes('utilities') || n.includes('power')) return '⚡';
    if (n.includes('gas') || n.includes('bharat')) return '🔥';
    if (n.includes('mobile') || n.includes('airtel') || n.includes('jio') || n.includes('phone')) return '📱';
    if (n.includes('tv') || n.includes('dish') || n.includes('entertainment')) return '📺';
    if (n.includes('rent') || n.includes('house')) return '🏠';
    if (n.includes('food')) return '🍔';
    if (n.includes('shopping')) return '🛍️';
    if (n.includes('travel')) return '✈️';
    if (n.includes('water')) return '💧';
    if (n.includes('internet') || n.includes('wifi')) return '🌐';
    if (n.includes('health') || n.includes('medical') || n.includes('hospital')) return '🏥';
    if (n.includes('education') || n.includes('school') || n.includes('college') || n.includes('tuition')) return '🎓';
    if (n.includes('transport') || n.includes('fuel') || n.includes('petrol') || n.includes('car')) return '🚗';
    if (n.includes('insurance')) return '🛡️';
    if (n.includes('subscript') || n.includes('netflix') || n.includes('spotify') || n.includes('prime')) return '📺';
    if (n.includes('grocer')) return '🛒';
    if (n.includes('dining') || n.includes('restaurant') || n.includes('cafe')) return '🍽️';
    if (n.includes('fitness') || n.includes('gym')) return '💪';
    if (n.includes('gift')) return '🎁';
    if (n.includes('invest') || n.includes('sip') || n.includes('mutual')) return '📈';
    return '📋';
  }

  getOverdueCount(): number {
    return this.bills.filter(b => b.status === 'OVERDUE').length;
  }

  getPaymentRate(): number {
    return this.bills.length > 0 ? (this.getPaidCount() / this.bills.length) * 100 : 0;
  }

  getDonutGradient(): string {
    const angle = Math.min(this.getPaymentRate(), 100) * 3.6;
    return `conic-gradient(#10b981 ${angle}deg, rgba(255,255,255,0.08) ${angle}deg)`;
  }

  getTopBillers(): { name: string; total: number; count: number }[] {
    const map = new Map<string, { total: number; count: number }>();
    this.bills.forEach(b => {
      const key = (b.billerName || '').toLowerCase();
      const entry = map.get(key) || { total: 0, count: 0 };
      entry.total += b.amount || 0;
      entry.count++;
      map.set(key, entry);
    });
    return Array.from(map.entries())
      .map(([name, data]) => ({ name, ...data }))
      .sort((a, b) => b.total - a.total)
      .slice(0, 5);
  }

  getRecentPaid(): any[] {
    return this.bills
      .filter(b => b.status === 'PAID')
      .sort((a, b) => (b.dueDate || '').localeCompare(a.dueDate || ''))
      .slice(0, 4);
  }
}
