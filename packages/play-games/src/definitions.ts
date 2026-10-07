export type LeaderboardTimeSpan = 'daily' | 'weekly' | 'allTime';

export type LeaderboardCollection = 'public' | 'friends';

export interface AuthResult {
  /**
   * Whether a player is signed in to Play Games Services.
   *
   * @since 0.1.0
   */
  isAuthenticated: boolean;

  /**
   * Google API status code. Only present when the SDK reported an error.
   *
   * `10` (DEVELOPER_ERROR) means the app is misconfigured: the SHA-1 of this
   * build or its package name is missing from the OAuth credentials in the
   * Play Console. `16` and `12501` mean the player cancelled.
   *
   * @since 0.1.0
   */
  errorCode?: number;

  /**
   * Error message from the SDK. Only present when the SDK reported an error.
   *
   * @since 0.1.0
   */
  errorMessage?: string;
}

export interface Player {
  /**
   * @since 0.1.0
   */
  playerId: string;

  /**
   * The name the player shows in Play Games.
   *
   * @since 0.1.0
   */
  displayName: string;

  /**
   * The player's Play Games title, if they have one.
   *
   * @since 0.1.0
   */
  title?: string;
}

export interface SubmitScoreOptions {
  /**
   * @since 0.1.0
   */
  leaderboardId: string;

  /**
   * Raw score, an integer. For time leaderboards it is in milliseconds and for
   * currency leaderboards in 1/1,000,000ths of the main unit.
   *
   * @since 0.1.0
   */
  score: number;

  /**
   * Wait for the server to confirm the score.
   *
   * With `false` the SDK queues the score, even offline, and the call resolves
   * straight away. With `true` the call resolves once the server has the score
   * and rejects when it cannot be reached; use it when you read the standing
   * right after submitting.
   *
   * @default false
   * @since 0.1.0
   */
  immediate?: boolean;
}

export interface LeaderboardOptions {
  /**
   * @since 0.1.0
   */
  leaderboardId: string;

  /**
   * @default 'allTime'
   * @since 0.1.0
   */
  timeSpan?: LeaderboardTimeSpan;

  /**
   * `'friends'` needs the player's permission to read their friends list.
   * Until they have granted it, `getLeaderboardStanding` rejects with code
   * `'26703'` (CONSENT_REQUIRED).
   *
   * @default 'public'
   * @since 0.1.0
   */
  collection?: LeaderboardCollection;
}

export interface LeaderboardStandingOptions extends LeaderboardOptions {
  /**
   * Skip the local cache and ask the server. The cached number of scores can
   * be stale, but Google rate-limits forced reloads: keep it for moments when
   * fresh data matters.
   *
   * @default false
   * @since 0.1.0
   */
  forceReload?: boolean;
}

export interface LeaderboardStanding {
  /**
   * The player's rank, or `null` when unknown (for example, no score in this
   * time span).
   *
   * @since 0.1.0
   */
  rank: number | null;

  /**
   * The player's raw score, or `null` when unknown.
   *
   * @since 0.1.0
   */
  score: number | null;

  /**
   * How many scores the leaderboard has in this time span and collection, or
   * `null` when unknown.
   *
   * @since 0.1.0
   */
  numScores: number | null;
}

export interface AchievementOptions {
  /**
   * @since 0.1.0
   */
  achievementId: string;
}

export interface AchievementStepsOptions {
  /**
   * @since 0.1.0
   */
  achievementId: string;

  /**
   * @since 0.1.0
   */
  steps: number;
}

export interface SaveGameOptions {
  /**
   * Unique name of the saved game: 1 to 100 characters from `a-z`, `A-Z`,
   * `0-9`, `-`, `.`, `_` and `~`.
   *
   * @since 0.1.0
   */
  name: string;

  /**
   * The contents, stored as UTF-8. Play allows up to 3 MB per saved game.
   *
   * @since 0.1.0
   */
  data: string;

  /**
   * Description shown in the Play Games saved games UI.
   *
   * @since 0.1.0
   */
  description?: string;
}

export interface LoadGameOptions {
  /**
   * @since 0.1.0
   */
  name: string;
}

export interface LoadGameResult {
  /**
   * The saved contents, or `null` when no saved game with that name exists.
   *
   * @since 0.1.0
   */
  data: string | null;
}

export interface PlayGamesPlugin {
  /**
   * Whether a player is signed in. Shows no UI and never rejects.
   *
   * The SDK tries a silent sign-in when the app starts, so this is the call
   * to make before drawing anything that depends on the session.
   *
   * @since 0.1.0
   */
  isAuthenticated(): Promise<AuthResult>;

  /**
   * Ask the player to sign in. This is the only call that shows Google's
   * sign-in UI, so make it from a button the player pressed. Never rejects:
   * a cancelled or failed sign-in resolves with `isAuthenticated: false`.
   *
   * @since 0.1.0
   */
  signIn(): Promise<AuthResult>;

  /**
   * The signed-in player. Rejects when nobody is signed in.
   *
   * @since 0.1.0
   */
  getCurrentPlayer(): Promise<Player>;

  /**
   * Open the Play Games app, or its store listing when it is not installed.
   *
   * Useful when `signIn()` cannot succeed because the device has no Google
   * account or the player has no Play Games profile: both are created there.
   *
   * @since 0.1.0
   */
  openPlayGamesApp(): Promise<void>;

  /**
   * Submit a score to a leaderboard.
   *
   * @since 0.1.0
   */
  submitScore(options: SubmitScoreOptions): Promise<void>;

  /**
   * The player's rank and score, and the number of scores in the
   * leaderboard, read together for one time span and collection.
   *
   * @since 0.1.0
   */
  getLeaderboardStanding(options: LeaderboardStandingOptions): Promise<LeaderboardStanding>;

  /**
   * Open Google's UI for one leaderboard. Resolves when the UI opens.
   *
   * @since 0.1.0
   */
  showLeaderboard(options: LeaderboardOptions): Promise<void>;

  /**
   * Open Google's UI listing every leaderboard of the game.
   *
   * @since 0.1.0
   */
  showAllLeaderboards(): Promise<void>;

  /**
   * Unlock an achievement. Queued by the SDK when offline.
   *
   * @since 0.1.0
   */
  unlockAchievement(options: AchievementOptions): Promise<void>;

  /**
   * Reveal a hidden achievement. Queued by the SDK when offline.
   *
   * @since 0.1.0
   */
  revealAchievement(options: AchievementOptions): Promise<void>;

  /**
   * Add steps to an incremental achievement. Queued by the SDK when offline.
   *
   * @since 0.1.0
   */
  incrementAchievement(options: AchievementStepsOptions): Promise<void>;

  /**
   * Set the absolute progress of an incremental achievement. Play ignores a
   * value lower than the one it already has, which makes this the safe choice
   * when your own save holds the real counter.
   *
   * @since 0.1.0
   */
  setAchievementSteps(options: AchievementStepsOptions): Promise<void>;

  /**
   * Open Google's achievements UI. Resolves when the UI opens.
   *
   * @since 0.1.0
   */
  showAchievements(): Promise<void>;

  /**
   * Write a saved game to the player's Google account, creating it if needed.
   * Conflicts between devices are resolved by keeping the most recently
   * modified copy.
   *
   * @since 0.1.0
   */
  saveGame(options: SaveGameOptions): Promise<void>;

  /**
   * Read a saved game. Resolves with `data: null` when it does not exist and
   * rejects when it could not be read.
   *
   * @since 0.1.0
   */
  loadGame(options: LoadGameOptions): Promise<LoadGameResult>;
}
