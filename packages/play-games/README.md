# @byvik/capacitor-play-games

Google Play Games Services **v2** for Capacitor: sign-in, leaderboards, achievements and cloud saved games.

- Built on `play-services-games-v2`, the SDK Google currently supports. No `GoogleApiClient`, no legacy Google Sign-In.
- Saved games (snapshots), so a player's progress follows their Google account across devices and reinstalls.
- Leaderboard standing: the player's rank and the number of scores in one call.
- Android only. On iOS and web every method rejects, so guard calls with `Capacitor.getPlatform() === 'android'`.

> **Status (0.1.0):** extracted from a game in production on Google Play. Since the extraction, the plugin has been run on a device only while signed out, to check the setup and error paths. The signed-in flows have not been re-run in this packaged form yet, and `incrementAchievement`, `revealAchievement`, `showAllLeaderboards` and the queued `submitScore` are new. Please open an issue if something breaks.

## Install

```bash
npm install @byvik/capacitor-play-games
npx cap sync
```

Requires Capacitor 8.

## Setup

1. In the [Play Console](https://play.google.com/console), open your game and go to **Play Games Services → Setup and management → Configuration**. Create the project and add an Android credential for every key that signs your app: the debug key, your upload key and the Play App Signing key. A missing SHA-1 is the usual reason sign-in fails with `errorCode: 10`.

2. Copy the **Project ID** shown at the top of that page into `android/app/src/main/res/values/strings.xml`:

   ```xml
   <string name="game_services_project_id" translatable="false">123456789012</string>
   ```

3. Reference it inside `<application>` in `android/app/src/main/AndroidManifest.xml`:

   ```xml
   <meta-data
       android:name="com.google.android.gms.games.APP_ID"
       android:value="@string/game_services_project_id" />
   ```

4. To use `saveGame` and `loadGame`, turn on **Saved Games** in the same configuration page.

While the game is unpublished, only accounts listed under **Testers** can sign in.

### Variables

Set these in `android/variables.gradle` to override the defaults:

- `playServicesGamesV2Version`: version of `com.google.android.gms:play-services-games-v2` (default: `20.1.2`)

## Usage

```ts
import { PlayGames } from '@byvik/capacitor-play-games';

// On launch: the SDK has already tried a silent sign-in.
const { isAuthenticated } = await PlayGames.isAuthenticated();

// From a "Sign in" button:
const result = await PlayGames.signIn();
if (!result.isAuthenticated) {
  console.warn('Not signed in', result.errorCode, result.errorMessage);
}

// Leaderboards
await PlayGames.submitScore({ leaderboardId: 'CgkI...', score: 4200 });
await PlayGames.showLeaderboard({ leaderboardId: 'CgkI...', timeSpan: 'daily' });
const { rank, numScores } = await PlayGames.getLeaderboardStanding({
  leaderboardId: 'CgkI...',
  timeSpan: 'daily',
});

// Achievements
await PlayGames.unlockAchievement({ achievementId: 'CgkI...' });
await PlayGames.setAchievementSteps({ achievementId: 'CgkI...', steps: 37 });
await PlayGames.showAchievements();

// Saved games
await PlayGames.saveGame({ name: 'progress', data: JSON.stringify(save) });
const { data } = await PlayGames.loadGame({ name: 'progress' });
if (data !== null) {
  const cloudSave = JSON.parse(data);
}
```

### Errors

`isAuthenticated()` and `signIn()` never reject; they resolve with `isAuthenticated: false`. Every other method rejects when Play Games reports a failure, and the error's `code` is the [Google API status code](https://developers.google.com/android/reference/com/google/android/gms/games/GamesClientStatusCodes) as a string.

## API

<docgen-index>

* [`isAuthenticated()`](#isauthenticated)
* [`signIn()`](#signin)
* [`getCurrentPlayer()`](#getcurrentplayer)
* [`openPlayGamesApp()`](#openplaygamesapp)
* [`submitScore(...)`](#submitscore)
* [`getLeaderboardStanding(...)`](#getleaderboardstanding)
* [`showLeaderboard(...)`](#showleaderboard)
* [`showAllLeaderboards()`](#showallleaderboards)
* [`unlockAchievement(...)`](#unlockachievement)
* [`revealAchievement(...)`](#revealachievement)
* [`incrementAchievement(...)`](#incrementachievement)
* [`setAchievementSteps(...)`](#setachievementsteps)
* [`showAchievements()`](#showachievements)
* [`saveGame(...)`](#savegame)
* [`loadGame(...)`](#loadgame)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### isAuthenticated()

```typescript
isAuthenticated() => Promise<AuthResult>
```

Whether a player is signed in. Shows no UI and never rejects.

The SDK tries a silent sign-in when the app starts, so this is the call
to make before drawing anything that depends on the session.

**Returns:** <code>Promise&lt;<a href="#authresult">AuthResult</a>&gt;</code>

**Since:** 0.1.0

--------------------


### signIn()

```typescript
signIn() => Promise<AuthResult>
```

Ask the player to sign in. This is the only call that shows Google's
sign-in UI, so make it from a button the player pressed. Never rejects:
a cancelled or failed sign-in resolves with `isAuthenticated: false`.

**Returns:** <code>Promise&lt;<a href="#authresult">AuthResult</a>&gt;</code>

**Since:** 0.1.0

--------------------


### getCurrentPlayer()

```typescript
getCurrentPlayer() => Promise<Player>
```

The signed-in player. Rejects when nobody is signed in.

**Returns:** <code>Promise&lt;<a href="#player">Player</a>&gt;</code>

**Since:** 0.1.0

--------------------


### openPlayGamesApp()

```typescript
openPlayGamesApp() => Promise<void>
```

Open the Play Games app, or its store listing when it is not installed.

Useful when `signIn()` cannot succeed because the device has no Google
account or the player has no Play Games profile: both are created there.

**Since:** 0.1.0

--------------------


### submitScore(...)

```typescript
submitScore(options: SubmitScoreOptions) => Promise<void>
```

Submit a score to a leaderboard.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#submitscoreoptions">SubmitScoreOptions</a></code> |

**Since:** 0.1.0

--------------------


### getLeaderboardStanding(...)

```typescript
getLeaderboardStanding(options: LeaderboardStandingOptions) => Promise<LeaderboardStanding>
```

The player's rank and score, and the number of scores in the
leaderboard, read together for one time span and collection.

| Param         | Type                                                                              |
| ------------- | --------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#leaderboardstandingoptions">LeaderboardStandingOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#leaderboardstanding">LeaderboardStanding</a>&gt;</code>

**Since:** 0.1.0

--------------------


### showLeaderboard(...)

```typescript
showLeaderboard(options: LeaderboardOptions) => Promise<void>
```

Open Google's UI for one leaderboard. Resolves when the UI opens.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#leaderboardoptions">LeaderboardOptions</a></code> |

**Since:** 0.1.0

--------------------


### showAllLeaderboards()

```typescript
showAllLeaderboards() => Promise<void>
```

Open Google's UI listing every leaderboard of the game.

**Since:** 0.1.0

--------------------


### unlockAchievement(...)

```typescript
unlockAchievement(options: AchievementOptions) => Promise<void>
```

Unlock an achievement. Queued by the SDK when offline.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#achievementoptions">AchievementOptions</a></code> |

**Since:** 0.1.0

--------------------


### revealAchievement(...)

```typescript
revealAchievement(options: AchievementOptions) => Promise<void>
```

Reveal a hidden achievement. Queued by the SDK when offline.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#achievementoptions">AchievementOptions</a></code> |

**Since:** 0.1.0

--------------------


### incrementAchievement(...)

```typescript
incrementAchievement(options: AchievementStepsOptions) => Promise<void>
```

Add steps to an incremental achievement. Queued by the SDK when offline.

| Param         | Type                                                                        |
| ------------- | --------------------------------------------------------------------------- |
| **`options`** | <code><a href="#achievementstepsoptions">AchievementStepsOptions</a></code> |

**Since:** 0.1.0

--------------------


### setAchievementSteps(...)

```typescript
setAchievementSteps(options: AchievementStepsOptions) => Promise<void>
```

Set the absolute progress of an incremental achievement. Play ignores a
value lower than the one it already has, which makes this the safe choice
when your own save holds the real counter.

| Param         | Type                                                                        |
| ------------- | --------------------------------------------------------------------------- |
| **`options`** | <code><a href="#achievementstepsoptions">AchievementStepsOptions</a></code> |

**Since:** 0.1.0

--------------------


### showAchievements()

```typescript
showAchievements() => Promise<void>
```

Open Google's achievements UI. Resolves when the UI opens.

**Since:** 0.1.0

--------------------


### saveGame(...)

```typescript
saveGame(options: SaveGameOptions) => Promise<void>
```

Write a saved game to the player's Google account, creating it if needed.
Conflicts between devices are resolved by keeping the most recently
modified copy.

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#savegameoptions">SaveGameOptions</a></code> |

**Since:** 0.1.0

--------------------


### loadGame(...)

```typescript
loadGame(options: LoadGameOptions) => Promise<LoadGameResult>
```

Read a saved game. Resolves with `data: null` when it does not exist and
rejects when it could not be read.

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#loadgameoptions">LoadGameOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#loadgameresult">LoadGameResult</a>&gt;</code>

**Since:** 0.1.0

--------------------


### Interfaces


#### AuthResult

| Prop                  | Type                 | Description                                                                                                                                                                                                                                                                    | Since |
| --------------------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----- |
| **`isAuthenticated`** | <code>boolean</code> | Whether a player is signed in to Play Games Services.                                                                                                                                                                                                                          | 0.1.0 |
| **`errorCode`**       | <code>number</code>  | Google API status code. Only present when the SDK reported an error. `10` (DEVELOPER_ERROR) means the app is misconfigured: the SHA-1 of this build or its package name is missing from the OAuth credentials in the Play Console. `16` and `12501` mean the player cancelled. | 0.1.0 |
| **`errorMessage`**    | <code>string</code>  | Error message from the SDK. Only present when the SDK reported an error.                                                                                                                                                                                                       | 0.1.0 |


#### Player

| Prop              | Type                | Description                                      | Since |
| ----------------- | ------------------- | ------------------------------------------------ | ----- |
| **`playerId`**    | <code>string</code> |                                                  | 0.1.0 |
| **`displayName`** | <code>string</code> | The name the player shows in Play Games.         | 0.1.0 |
| **`title`**       | <code>string</code> | The player's Play Games title, if they have one. | 0.1.0 |


#### SubmitScoreOptions

| Prop                | Type                 | Description                                                                                                                                                                                                                                                                                      | Default            | Since |
| ------------------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------ | ----- |
| **`leaderboardId`** | <code>string</code>  |                                                                                                                                                                                                                                                                                                  |                    | 0.1.0 |
| **`score`**         | <code>number</code>  | Raw score, an integer. For time leaderboards it is in milliseconds and for currency leaderboards in 1/1,000,000ths of the main unit.                                                                                                                                                             |                    | 0.1.0 |
| **`immediate`**     | <code>boolean</code> | Wait for the server to confirm the score. With `false` the SDK queues the score, even offline, and the call resolves straight away. With `true` the call resolves once the server has the score and rejects when it cannot be reached; use it when you read the standing right after submitting. | <code>false</code> | 0.1.0 |


#### LeaderboardStanding

| Prop            | Type                        | Description                                                                                   | Since |
| --------------- | --------------------------- | --------------------------------------------------------------------------------------------- | ----- |
| **`rank`**      | <code>number \| null</code> | The player's rank, or `null` when unknown (for example, no score in this time span).          | 0.1.0 |
| **`score`**     | <code>number \| null</code> | The player's raw score, or `null` when unknown.                                               | 0.1.0 |
| **`numScores`** | <code>number \| null</code> | How many scores the leaderboard has in this time span and collection, or `null` when unknown. | 0.1.0 |


#### LeaderboardStandingOptions

| Prop              | Type                 | Description                                                                                                                                                            | Default            | Since |
| ----------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ | ----- |
| **`forceReload`** | <code>boolean</code> | Skip the local cache and ask the server. The cached number of scores can be stale, but Google rate-limits forced reloads: keep it for moments when fresh data matters. | <code>false</code> | 0.1.0 |


#### LeaderboardOptions

| Prop                | Type                                                                    | Default                | Since |
| ------------------- | ----------------------------------------------------------------------- | ---------------------- | ----- |
| **`leaderboardId`** | <code>string</code>                                                     |                        | 0.1.0 |
| **`timeSpan`**      | <code><a href="#leaderboardtimespan">LeaderboardTimeSpan</a></code>     | <code>'allTime'</code> | 0.1.0 |
| **`collection`**    | <code><a href="#leaderboardcollection">LeaderboardCollection</a></code> | <code>'public'</code>  | 0.1.0 |


#### AchievementOptions

| Prop                | Type                | Since |
| ------------------- | ------------------- | ----- |
| **`achievementId`** | <code>string</code> | 0.1.0 |


#### AchievementStepsOptions

| Prop                | Type                | Since |
| ------------------- | ------------------- | ----- |
| **`achievementId`** | <code>string</code> | 0.1.0 |
| **`steps`**         | <code>number</code> | 0.1.0 |


#### SaveGameOptions

| Prop              | Type                | Description                                                                                         | Since |
| ----------------- | ------------------- | --------------------------------------------------------------------------------------------------- | ----- |
| **`name`**        | <code>string</code> | Unique name of the saved game: 1 to 100 characters from `a-z`, `A-Z`, `0-9`, `-`, `.`, `_` and `~`. | 0.1.0 |
| **`data`**        | <code>string</code> | The contents, stored as UTF-8. Play allows up to 3 MB per saved game.                               | 0.1.0 |
| **`description`** | <code>string</code> | Description shown in the Play Games saved games UI.                                                 | 0.1.0 |


#### LoadGameResult

| Prop       | Type                        | Description                                                             | Since |
| ---------- | --------------------------- | ----------------------------------------------------------------------- | ----- |
| **`data`** | <code>string \| null</code> | The saved contents, or `null` when no saved game with that name exists. | 0.1.0 |


#### LoadGameOptions

| Prop       | Type                | Since |
| ---------- | ------------------- | ----- |
| **`name`** | <code>string</code> | 0.1.0 |


### Type Aliases


#### LeaderboardTimeSpan

<code>'daily' | 'weekly' | 'allTime'</code>


#### LeaderboardCollection

<code>'public' | 'friends'</code>

</docgen-api>

## License

MIT
