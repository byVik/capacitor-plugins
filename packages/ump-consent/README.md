# @byvik/capacitor-ump-consent

Google's User Messaging Platform (UMP) consent form for Capacitor, with the answer translated to **Consent Mode v2**.

- Standalone: it does not bundle or require an AdMob plugin, so it works with whatever ads integration you already have.
- Reads the TCF answer UMP stores and maps it to the four Consent Mode signals.
- Applies those signals to Firebase Analytics when your app includes it, and returns them either way.
- Never rejects: a network error or a form that fails to load cannot keep your app from starting.
- Android only. On iOS and web every method rejects, so guard calls with `Capacitor.getPlatform() === 'android'`.

This plugin is a tool, not legal advice. You are responsible for how your app handles consent.

## Install

```bash
npm install @byvik/capacitor-ump-consent
npx cap sync
```

Requires Capacitor 8.

## Setup

1. Create a consent message for your app in AdMob under **Privacy & messaging**.

2. UMP finds that message through your AdMob app ID. If your ads integration has not already added it, put it inside `<application>` in `android/app/src/main/AndroidManifest.xml`:

   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
   ```

3. If you use Firebase Analytics, deny everything by default in the same place:

   ```xml
   <meta-data android:name="google_analytics_default_allow_analytics_storage" android:value="false" />
   <meta-data android:name="google_analytics_default_allow_ad_storage" android:value="false" />
   <meta-data android:name="google_analytics_default_allow_ad_user_data" android:value="false" />
   <meta-data android:name="google_analytics_default_allow_ad_personalization_signals" android:value="false" />
   ```

   The defaults have to live in the manifest. Firebase starts with the process, before any plugin code runs, so defaults set from JavaScript would leave a window in which the SDK is already collecting with its permissive factory settings.

### Variables

Set these in `android/variables.gradle` to override the defaults:

- `umpVersion`: version of `com.google.android.ump:user-messaging-platform` (default: `3.1.0`)
- `firebaseAnalyticsVersion`: version of `com.google.firebase:firebase-analytics` the plugin compiles against (default: `22.1.2`). The plugin does not add Firebase to your app.

## Usage

```ts
import { UmpConsent } from '@byvik/capacitor-ump-consent';

// On every launch, before initializing ads.
const state = await UmpConsent.requestConsent();
if (state.canRequestAds) {
  // initialize your ads SDK
}

// In your settings screen.
if (state.privacyOptionsRequired) {
  showPrivacyRow(() => UmpConsent.showPrivacyOptionsForm());
}
```

### Consent Mode

After each request or change, the plugin reads what UMP stored and derives:

| Signal               | Granted when                    |
| -------------------- | ------------------------------- |
| `analytics_storage`  | TCF purpose 1 is consented      |
| `ad_storage`         | TCF purpose 1 is consented      |
| `ad_user_data`       | TCF purposes 1 and 7 are consented |
| `ad_personalization` | TCF purposes 3 and 4 are consented |

The three ad signals follow the mapping Google documents for TCF. TCF has no purpose for analytics; tying `analytics_storage` to purpose 1 is this plugin's choice.

Where GDPR does not apply (UMP reports that no consent is required), all four are granted. With the manifest defaults above, users outside the regulated regions would otherwise never be measured.

Until UMP knows whether the user has to be asked, for example on a first launch without network, nothing is granted and nothing is applied: your manifest defaults stay in force.

Only TCF (GDPR) messages are mapped. Other message types, such as US state regulations, are not translated to Consent Mode: for those users the four signals are granted.

The same signals are returned in `ConsentState.consentMode`. To apply them yourself, turn the automatic step off:

<docgen-config>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

| Prop                      | Type                 | Description                                                                                                                                                                                                                                                   | Default           | Since |
| ------------------------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------- | ----- |
| **`firebaseConsentMode`** | <code>boolean</code> | Apply the consent to Firebase Analytics (Consent Mode v2) every time the consent is requested or changed. Has no effect in apps that do not include Firebase Analytics. Set it to `false` if you apply Consent Mode yourself from `ConsentState.consentMode`. | <code>true</code> | 0.1.0 |

### Examples

In `capacitor.config.json`:

```json
{
  "plugins": {
    "UmpConsent": {
      "firebaseConsentMode": false
    }
  }
}
```

In `capacitor.config.ts`:

```ts
/// <reference types="@byvik/capacitor-ump-consent" />

import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  plugins: {
    UmpConsent: {
      firebaseConsentMode: false,
    },
  },
};

export default config;
```

</docgen-config>

### Testing

Run the app once, find the line UMP prints to logcat with your device's hashed ID, and pass it:

```ts
await UmpConsent.reset();
await UmpConsent.requestConsent({
  debugGeography: 'eea',
  testDeviceIds: ['33BE2250B43518CCDA7DE426D04EE231'],
});
```

## API

<docgen-index>

* [`requestConsent(...)`](#requestconsent)
* [`showPrivacyOptionsForm()`](#showprivacyoptionsform)
* [`getConsentState()`](#getconsentstate)
* [`reset()`](#reset)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### requestConsent(...)

```typescript
requestConsent(options?: RequestConsentOptions | undefined) => Promise<ConsentState>
```

Update the consent information and show the consent form if it is
required. Call it on every app launch, before initializing any ads SDK.

Never rejects: when the request or the form fails it resolves with the
stored state and an `error` message.

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code><a href="#requestconsentoptions">RequestConsentOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#consentstate">ConsentState</a>&gt;</code>

**Since:** 0.1.0

--------------------


### showPrivacyOptionsForm()

```typescript
showPrivacyOptionsForm() => Promise<ConsentState>
```

Show the privacy options form so the user can change their choice.
Offer it when `privacyOptionsRequired` is `true`. Never rejects.

**Returns:** <code>Promise&lt;<a href="#consentstate">ConsentState</a>&gt;</code>

**Since:** 0.1.0

--------------------


### getConsentState()

```typescript
getConsentState() => Promise<ConsentState>
```

The stored consent state, without any network request or UI.

**Returns:** <code>Promise&lt;<a href="#consentstate">ConsentState</a>&gt;</code>

**Since:** 0.1.0

--------------------


### reset()

```typescript
reset() => Promise<void>
```

Forget the stored consent so the form shows again. For testing only.

**Since:** 0.1.0

--------------------


### Interfaces


#### ConsentState

| Prop                         | Type                                                              | Description                                                                                                                                          | Since |
| ---------------------------- | ----------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| **`canRequestAds`**          | <code>boolean</code>                                              | Whether ads can be requested. Initialize your ads SDK only when this is `true`.                                                                      | 0.1.0 |
| **`status`**                 | <code><a href="#consentstatus">ConsentStatus</a></code>           | UMP's consent status.                                                                                                                                | 0.1.0 |
| **`privacyOptionsRequired`** | <code>boolean</code>                                              | Whether the app must offer a way to reopen the privacy options form, for example a row in its settings screen that calls `showPrivacyOptionsForm()`. | 0.1.0 |
| **`gdprApplies`**            | <code>boolean</code>                                              | Whether GDPR applies to this user, as stored by UMP in `IABTCF_gdprApplies`.                                                                         | 0.1.0 |
| **`purposeConsents`**        | <code>string</code>                                               | The raw `IABTCF_PurposeConsents` string: character `n - 1` is `'1'` when TCF purpose `n` is consented. Empty when there is none.                     | 0.1.0 |
| **`consentMode`**            | <code><a href="#consentmodesignals">ConsentModeSignals</a></code> | The answer translated to Consent Mode v2. Everything is `false` until UMP knows whether this user has to be asked.                                   | 0.1.0 |
| **`error`**                  | <code>string</code>                                               | Message of the error that interrupted the request or the form, if any. The rest of the state is still valid: it reflects what UMP has stored.        | 0.1.0 |


#### ConsentModeSignals

| Prop                    | Type                 | Since |
| ----------------------- | -------------------- | ----- |
| **`analyticsStorage`**  | <code>boolean</code> | 0.1.0 |
| **`adStorage`**         | <code>boolean</code> | 0.1.0 |
| **`adUserData`**        | <code>boolean</code> | 0.1.0 |
| **`adPersonalization`** | <code>boolean</code> | 0.1.0 |


#### RequestConsentOptions

| Prop                          | Type                                                      | Description                                                                                                            | Default            | Since |
| ----------------------------- | --------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | ------------------ | ----- |
| **`tagForUnderAgeOfConsent`** | <code>boolean</code>                                      | Tell UMP the user is under the age of consent.                                                                         | <code>false</code> | 0.1.0 |
| **`debugGeography`**          | <code><a href="#debuggeography">DebugGeography</a></code> | Make a test device behave as if it were in this geography. Only applies to devices listed in `testDeviceIds`.          |                    | 0.1.0 |
| **`testDeviceIds`**           | <code>string[]</code>                                     | Hashed IDs of the test devices. UMP prints the ID of the current device to logcat the first time consent is requested. |                    | 0.1.0 |


### Type Aliases


#### ConsentStatus

<code>'unknown' | 'notRequired' | 'required' | 'obtained'</code>


#### DebugGeography

<code>'disabled' | 'eea' | 'regulatedUsState' | 'other'</code>

</docgen-api>

## License

MIT
