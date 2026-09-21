import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class BillService {

  private baseUrl = '/api/bills';

  constructor(private http: HttpClient) {}

  getBills() {
    return this.http.get(this.baseUrl);
  }

  createBill(data: any) {
    return this.http.post(this.baseUrl, data);
  }

  payBill(id: number, method: string) {
    return this.http.patch(
      `${this.baseUrl}/${id}/pay?method=${method}`, {}
    );
  }
}
