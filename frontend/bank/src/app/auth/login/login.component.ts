// import { Component } from '@angular/core';
// import { Router } from '@angular/router';
// import { FormsModule } from '@angular/forms';
// import { CommonModule } from '@angular/common';
// import { AuthService } from '../auth.service';

// @Component({
//     standalone: true,
//     selector: 'app-login',
//     imports: [CommonModule, FormsModule],
//     templateUrl: './login.component.html',
//     styleUrls: ['./login.component.css']
// })
// export class LoginComponent {

//   email = '';
//   password = '';

//   // ✅ CHARACTER CAPTCHA
//   captchaText = '';
//   captchaInput = '';

//   constructor(
//     private auth: AuthService,
//     private router: Router
//   ) {
//     this.generateCaptcha();
//   }

//   generateCaptcha() {
//     const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz@#$%&*0123456789';
//     this.captchaText = '';

//     for (let i = 0; i < 5; i++) {
//       this.captchaText += chars.charAt(Math.floor(Math.random() * chars.length));
//     }
//   }

//   login() {

//     // ✅ CAPTCHA VALIDATION
//     if (this.captchaInput.toUpperCase() !== this.captchaText) {
//       alert('Invalid CAPTCHA');
//       this.generateCaptcha();
//       this.captchaInput = '';
//       return;
//     }

//     this.auth.login({ email: this.email, password: this.password })
//       .subscribe({
//         next: token => {
//           sessionStorage.setItem('token', token);
//           alert('Login successful');
//           this.router.navigate(['/dashboard']);
//         },
//         error: err => {
//           if (err.status === 403) alert('Account inactive');
//           else alert('Invalid credentials');

//           this.generateCaptcha();
//           this.captchaInput = '';
//         }
//       });
//   }

//   goToRegister() {
//     this.router.navigate(['/register']);
//   }

//   // ✅ Optional refresh button
//   refreshCaptcha() {
//     this.generateCaptcha();
//     this.captchaInput = '';
//   }
// }


import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  email: string = '';
  password: string = '';

  // ✅ CAPTCHA
  captchaText: string = '';
  captchaInput: string = '';

  showPrivacyPolicy: boolean = false;

  constructor(
    private auth: AuthService,
    private router: Router
  ) {
    this.generateCaptcha();
  }

  // ✅ GENERATE CAPTCHA
  generateCaptcha(): void {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz@#$%&*0123456789';
    this.captchaText = '';

    for (let i = 0; i < 5; i++) {
      this.captchaText += chars.charAt(Math.floor(Math.random() * chars.length));
    }
  }

  // ✅ LOGIN FUNCTION
  login(): void {

    // ✅ FIXED CAPTCHA VALIDATION
    if (this.captchaInput.trim().toLowerCase() !== this.captchaText.toLowerCase()) {
      alert('Invalid CAPTCHA');
      this.generateCaptcha();
      this.captchaInput = '';
      return;
    }

    // ✅ API CALL
    this.auth.login({ email: this.email, password: this.password })
      .subscribe({
        next: (token: string) => {
          sessionStorage.setItem('token', token);
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          if (err.status === 403) {
            alert('Account inactive');
          } else {
            alert('Invalid credentials');
          }

          // reset captcha after fail
          this.generateCaptcha();
          this.captchaInput = '';
        }
      });
  }

  // ✅ NAVIGATION
  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  goToForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }

  // ✅ REFRESH CAPTCHA BUTTON
  refreshCaptcha(): void {
    this.generateCaptcha();
    this.captchaInput = '';
  }
}
