import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = '/api/users';

  constructor(private http: HttpClient) {}

  private getHttpOptions() {
    const token = sessionStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      })
    };
  }

  // ✅ Use existing backend endpoint /me instead of /profile
  getProfile(): Observable<any> {
    return this.http.get(`${this.baseUrl}/me`, this.getHttpOptions());
  }

  // ✅ Keep using /profile for updates (add this to backend)
  updateProfile(profileData: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/profile`, profileData, this.getHttpOptions());
  }

  uploadProfilePhoto(formData: FormData): Observable<any> {
    const token = sessionStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': token ? `Bearer ${token}` : ''
    });
    return this.http.post(`${this.baseUrl}/profile/photo`, formData, { headers });
  }
}