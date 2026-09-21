import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RewardService } from '../user/reward.service';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-rewards',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rewards.component.html',
  styleUrls: ['./rewards.component.css']
})
export class RewardsComponent implements OnInit {

  points = 0;
  history: any[] = [];
  loading = true;

  constructor(private service: RewardService, private auth: AuthService) {}

  ngOnInit() {
    const userId = this.auth.getUserId();
    if (userId) {
      this.service.getRewards(userId).subscribe({
        next: (res: any) => {
          this.points = res?.balance ?? res?.pointsBalance ?? 0;
          this.history = res?.history ?? [];
          this.loading = false;
        },
        error: () => {
          this.points = 0;
          this.loading = false;
        }
      });
    }
  }

  get tier(): string {
    if (this.points >= 500) return 'PLATINUM';
    if (this.points >= 200) return 'GOLD';
    if (this.points >= 50) return 'SILVER';
    return 'BRONZE';
  }

  get tierColor(): string {
    if (this.points >= 500) return '#6366f1';
    if (this.points >= 200) return '#f59e0b';
    if (this.points >= 50) return '#94a3b8';
    return '#cd7f32';
  }

  get nextTier(): string {
    if (this.points >= 500) return 'Max Tier Achieved! 🎉';
    if (this.points >= 200) return (500 - this.points) + ' pts to Platinum';
    if (this.points >= 50) return (200 - this.points) + ' pts to Gold';
    return (50 - this.points) + ' pts to Silver';
  }

  getTierIcon(): string {
    if (this.points >= 500) return '💎';
    if (this.points >= 200) return '🥇';
    if (this.points >= 50) return '🥈';
    return '🥉';
  }

  getNextTierName(): string {
    if (this.points >= 500) return 'Max';
    if (this.points >= 200) return 'Platinum';
    if (this.points >= 50) return 'Gold';
    return 'Silver';
  }

  getProgressPercentage(): number {
    if (this.points >= 500) return 100;
    if (this.points >= 200) return ((this.points - 200) / 300) * 100;
    if (this.points >= 50) return ((this.points - 50) / 150) * 100;
    return (this.points / 50) * 100;
  }

  getPointsRingGradient(): string {
    const pct = Math.min((this.points / 500) * 100, 100);
    const angle = pct * 3.6;
    return `conic-gradient(${this.tierColor} ${angle}deg, rgba(255,255,255,0.08) ${angle}deg)`;
  }
}