import { WebPlugin } from '@capacitor/core';

import type { AuthResult, LeaderboardStanding, LoadGameResult, PlayGamesPlugin, Player } from './definitions';

const UNAVAILABLE = 'Play Games Services is only available on Android.';

export class PlayGamesWeb extends WebPlugin implements PlayGamesPlugin {
  async isAuthenticated(): Promise<AuthResult> {
    throw this.unavailable(UNAVAILABLE);
  }

  async signIn(): Promise<AuthResult> {
    throw this.unavailable(UNAVAILABLE);
  }

  async getCurrentPlayer(): Promise<Player> {
    throw this.unavailable(UNAVAILABLE);
  }

  async openPlayGamesApp(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async submitScore(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async getLeaderboardStanding(): Promise<LeaderboardStanding> {
    throw this.unavailable(UNAVAILABLE);
  }

  async showLeaderboard(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async showAllLeaderboards(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async unlockAchievement(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async revealAchievement(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async incrementAchievement(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async setAchievementSteps(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async showAchievements(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async saveGame(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }

  async loadGame(): Promise<LoadGameResult> {
    throw this.unavailable(UNAVAILABLE);
  }
}
