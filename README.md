# Capacitor plugins by byVik

Small Android plugins for [Capacitor](https://capacitorjs.com) 8, extracted from a game in production on Google Play.

| Package | What it does |
| --- | --- |
| [`@byvik/capacitor-play-games`](packages/play-games) | Google Play Games Services v2: sign-in, leaderboards, achievements and cloud saved games. |
| [`@byvik/capacitor-ump-consent`](packages/ump-consent) | Google UMP consent form, with the answer mapped to Consent Mode v2. No AdMob plugin required. |
| [`@byvik/capacitor-fully-drawn`](packages/fully-drawn) | Reports time to full display to Android vitals (`Activity.reportFullyDrawn()`). |

All three are Android only. Each package has its own README with setup, usage and the full API.

## Development

Every package is self-contained; there is no workspace to bootstrap.

```bash
cd packages/play-games
npm install
npm run build     # TypeScript, bundles and the API section of the README
npm run lint
npm run fmt
```

To compile the Android side, with `ANDROID_HOME` set and JDK 21:

```bash
cd android
./gradlew build
```

## Publishing

From the package folder, after bumping `version` in its `package.json`:

```bash
npm publish
```

`prepublishOnly` builds first, and `publishConfig` already marks the scoped packages as public.

## License

[MIT](LICENSE)
