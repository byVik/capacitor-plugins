# @byvik/capacitor-fully-drawn

Tell Android when your Capacitor app is really ready, so Android vitals measures the startup your users actually wait for.

Android times a cold start up to the first frame the activity draws. In a Capacitor app that frame is an empty WebView: the splash or loading screen shows up fast, and the first usable screen comes later. The startup time in the Play Console therefore measures something your users do not perceive as "started".

[`Activity.reportFullyDrawn()`](https://developer.android.com/reference/android/app/Activity#reportFullyDrawn()) is the official way to mark the moment the content is usable. Only your web code knows when that is, and this plugin lets it say so. Once reported, Android tracks a second figure, time to full display, next to time to initial display.

## Install

```bash
npm install @byvik/capacitor-fully-drawn
npx cap sync
```

Requires Capacitor 8. There is nothing to configure.

## Usage

Call it once, when the first real screen is on display. Not when the page loads: at that point your app still has to render.

```ts
import { FullyDrawn } from '@byvik/capacitor-fully-drawn';

function showMainMenu() {
  renderMenu();
  requestAnimationFrame(() => FullyDrawn.reportFullyDrawn());
}
```

- Only the first call of each launch is reported. Later calls do nothing.
- It never rejects. A measurement should not be able to break a launch.
- On iOS and web it is a no-op, so no platform check is needed.

To check it, filter logcat by `ActivityTaskManager` and look for a `Fully drawn` line with the elapsed time.

## API

<docgen-index>

* [`reportFullyDrawn()`](#reportfullydrawn)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### reportFullyDrawn()

```typescript
reportFullyDrawn() => Promise<void>
```

Tell Android the app is fully drawn and usable. Call it once, when the
first real screen is on display.

Only the first call of each launch is reported; later calls do nothing.
It never rejects, and on iOS and web it is a no-op, so it is safe to call
unconditionally.

**Since:** 0.1.0

--------------------

</docgen-api>

## License

MIT
