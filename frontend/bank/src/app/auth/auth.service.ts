import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private API = '/api/auth';

  constructor(private http: HttpClient) {}

  // ✅ LOGIN
  login(data: { email: string; password: string }) {
    return this.http.post(
      `${this.API}/login`,
      data,
      { responseType: 'text' }
    );
  }

  // ✅ REGISTER
  register(data: any) {
  return this.http.post<any>(
    '/api/auth/register',
    data
  );
}

  // ✅ FORGOT PASSWORD - Request OTP
  forgotPassword(email: string) {
    return this.http.post(
      `${this.API}/forgot-password`,
      { email },
      { responseType: 'text' }
    );
  }

  // ✅ RESET PASSWORD with OTP
  resetPassword(email: string, otp: string, newPassword: string) {
    return this.http.post(
      `${this.API}/reset-password`,
      { email, otp, newPassword },
      { responseType: 'text' }
    );
  }


  // ✅ TOKEN HANDLING
  saveToken(token: string) {
    sessionStorage.setItem('token', token);
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  logout() {
    sessionStorage.clear();
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // ✅ SAFE ROLE EXTRACTION FROM JWT
  getRole(): string | null {
    try {
      const token = this.getToken();
      if (!token) return null;

      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.role || null;
    } catch {
      this.logout();
      return null;
    }
  }

  // ✅ SAFE USER ID EXTRACTION FROM JWT
  getUserId(): number | null {
    try {
      const token = this.getToken();
      if (!token) return null;

      const payload = JSON.parse(atob(token.split('.')[1]));
      const id = payload?.id;
      const parsed = typeof id === 'number' ? id : Number(id);
      return Number.isFinite(parsed) ? parsed : null;
    } catch {
      this.logout();
      return null;
    }
  }
  
}
