import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { LoanService } from './loan.service';

@Component({
  standalone: true,
  selector: 'app-loan-apply',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './apply.component.html'
})
export class ApplyComponent implements OnInit {

  products: any[] = [];
  selectedProduct: any = null;
  form: FormGroup;

  constructor(private loanService: LoanService, private fb: FormBuilder) {
    this.form = this.fb.group({
      productId: [null, Validators.required],
      amount: [null, [Validators.required, Validators.min(1)]],
      tenure: [null, Validators.required]
    });
  }

  ngOnInit() {
    this.loadProducts();
    this.form.get('productId')?.valueChanges.subscribe(value => {
      this.setSelectedProduct(value);
    });
  }

  loadProducts() {
    this.loanService.getProducts().subscribe((res: any) => this.products = res || []);
  }

  private setSelectedProduct(rawValue: any) {
    const productId = Number(rawValue);
    const p = this.products.find(x => Number(x.id) === productId);
    this.selectedProduct = p || null;

    if (!p) return;

    // update validators for amount
    const min = p.minAmount ?? 0;
    const max = p.maxAmount ?? Number.MAX_SAFE_INTEGER;
    const amountCtrl = this.form.get('amount');
    amountCtrl?.setValidators([Validators.required, Validators.min(min), Validators.max(max)]);
    amountCtrl?.updateValueAndValidity();
  }

  submit() {
    if (this.form.invalid) { alert('Please fix form errors'); return; }

    const payload = {
      productId: Number(this.form.value.productId),
      amount: Number(this.form.value.amount),
      tenure: Number(this.form.value.tenure)
    };

    this.loanService.applyLoan(payload).subscribe({
      next: (res) => { alert('✅ Application submitted'); this.form.reset(); this.selectedProduct = null; },
      error: (err) => {
        console.error(err);
        const message = err?.error?.message || err?.error?.error || err?.error || err?.message || 'Failed to submit';
        alert('❌ ' + message);
      }
    });
  }
}
