import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup, AbstractControl } from '@angular/forms';
import { LoanService } from '../loans/loan.service';

function maxGreaterThanMin(control: AbstractControl) {
  const min = control.get('minAmount')?.value;
  const max = control.get('maxAmount')?.value;
  if (min != null && max != null && Number(max) <= Number(min)) {
    return { maxNotGreater: true };
  }
  return null;
}

@Component({
  standalone: true,
  selector: 'app-loan-products',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './loan-products.component.html'
})
export class LoanProductsComponent implements OnInit {

  form: FormGroup;
  products: any[] = [];

  constructor(private fb: FormBuilder, private loanService: LoanService) {
    this.form = this.fb.group({
      productName: ['', Validators.required],
      minAmount: [null, [Validators.required, Validators.min(1)]],
      maxAmount: [null, [Validators.required, Validators.min(1)]],
      annualInterestRate: [null, [Validators.required, Validators.min(0)]],
      allowedTenures: ['']
    }, { validators: maxGreaterThanMin });
  }

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.loanService.getProducts().subscribe((res: any) => this.products = res || []);
  }

  submit() {
    if (this.form.invalid) return alert('Fix validation errors');

    const raw = this.form.value;
    const tenures = (raw.allowedTenures || '')
      .split(',')
      .map((s: string) => s.trim())
      .filter((s: string) => s.length)
      .map((s: string) => Number(s));

    const payload = {
      productName: raw.productName,
      minAmount: raw.minAmount,
      maxAmount: raw.maxAmount,
      annualInterestRate: raw.annualInterestRate,
      allowedTenures: tenures
    };

    this.loanService.createProduct(payload).subscribe({
      next: () => { alert('Product created'); this.form.reset(); this.loadProducts(); },
      error: (err) => alert(err?.error || 'Failed to create')
    });
  }
}
