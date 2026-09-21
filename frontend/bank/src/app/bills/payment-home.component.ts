import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-payment-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-home.component.html',
  styleUrls: ['./payment-home.component.css']
})
export class PaymentHomeComponent {

  selectedService: string = '';

  selectService(service: string) {
    this.selectedService = service;
  }
}