import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class RewardService {

  private baseUrl = '/api/rewards';

  constructor(private http: HttpClient) {}

  getRewards(userId: number) {
    return this.http.get(`${this.baseUrl}/${userId}`);
  }
}