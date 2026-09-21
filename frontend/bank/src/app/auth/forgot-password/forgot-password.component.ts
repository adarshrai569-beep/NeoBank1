import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';

@Component({
  standalone: true,
  selector: 'app-forgot-password',
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {

  email: string = '';
  otp: string = '';
  newPassword: string = '';
  confirmPassword: string = '';

  step: number = 1;
  loading: boolean = false;
  message: string = '';
  error: string = '';
  showOtpNotification: boolean = false;
  receivedOtp: string = '';

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  // Step 1: Request OTP
  requestOtp(): void {
    if (!this.email) {
      this.error = 'Please enter your email';
      return;
    }

    this.loading = true;
    this.error = '';
    this.message = '';

    this.auth.forgotPassword(this.email).subscribe({
      next: (otp: string) => {
        this.loading = false;
        this.receivedOtp = otp;
        this.showOtpNotification = true;
        this.step = 2;
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error || 'Email not found';
      }
    });
  }

  dismissNotification(): void {
    this.showOtpNotification = false;
  }

  // Step 2: Reset password with OTP
  resetPassword(): void {
    if (!this.otp || !this.newPassword || !this.confirmPassword) {
      this.error = 'All fields are required';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.error = 'Passwords do not match';
      return;
    }

    if (this.newPassword.length < 6) {
      this.error = 'Password must be at least 6 characters';
      return;
    }

    this.loading = true;
    this.error = '';
    this.message = '';

    this.auth.resetPassword(this.email, this.otp, this.newPassword).subscribe({
      next: (res: string) => {
        this.loading = false;
        alert('Password reset successful! Please login.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error || 'Invalid OTP or reset failed';
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
