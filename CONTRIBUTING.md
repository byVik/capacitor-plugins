# Contributing

Bug reports, fixes and new features are welcome. For anything larger than a fix, open an issue first so we can agree on the approach before you write the code.

## Setup

You need Node.js 20 or newer. To compile the Android side you also need JDK 21 and the Android SDK, with `ANDROID_HOME` set.

Every package under `packages/` is self-contained:

```bash
cd packages/play-games
npm install
```

## Before opening a pull request

From the package you changed:

```bash
npm run fmt       # format TypeScript and Java
npm run lint
npm run build     # also regenerates the API section of the README
cd android && ./gradlew build
```

- The API section of each README is generated from the JSDoc in `src/definitions.ts`. Edit the comments there, not the README.
- Keep the plugins Android only unless the pull request adds a working iOS implementation.
- Say in the pull request whether you ran the change on a device, and on which Android version.

## Reporting a bug

Include the plugin version, the Capacitor version, the Android version and device, and the relevant logcat output.

## License

By contributing you agree that your contributions are licensed under the [MIT License](LICENSE).
