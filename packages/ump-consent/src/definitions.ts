/// <reference types="@capacitor/cli" />

declare module '@capacitor/cli' {
  export interface PluginsConfig {
    UmpConsent?: {
      /**
       * Apply the consent to Firebase Analytics (Consent Mode v2) every time
       * the consent is requested or changed. Has no effect in apps that do not
       * include Firebase Analytics.
       *
       * Set it to `false` if you apply Consent Mode yourself from
       * `ConsentState.consentMode`.
       *
       * @since 0.1.0
       * @default true
       * @example false
       */
      firebaseConsentMode?: boolean;
    };
  }
}

export type ConsentStatus = 'unknown' | 'notRequired' | 'required' | 'obtained';

export type DebugGeography = 'disabled' | 'eea' | 'regulatedUsState' | 'other';

export interface RequestConsentOptions {
  /**
   * Tell UMP the user is under the age of consent.
   *
   * @default false
   * @since 0.1.0
   */
  tagForUnderAgeOfConsent?: boolean;

  /**
   * Make a test device behave as if it were in this geography. Only applies to
   * devices listed in `testDeviceIds`.
   *
   * @since 0.1.0
   */
  debugGeography?: DebugGeography;

  /**
   * Hashed IDs of the test devices. UMP prints the ID of the current device to
   * logcat the first time consent is requested.
   *
   * @since 0.1.0
   */
  testDeviceIds?: string[];
}

export interface ConsentModeSignals {
  /**
   * @since 0.1.0
   */
  analyticsStorage: boolean;

  /**
   * @since 0.1.0
   */
  adStorage: boolean;

  /**
   * @since 0.1.0
   */
  adUserData: boolean;

  /**
   * @since 0.1.0
   */
  adPersonalization: boolean;
}

export interface ConsentState {
  /**
   * Whether ads can be requested. Initialize your ads SDK only when this is
   * `true`.
   *
   * @since 0.1.0
   */
  canRequestAds: boolean;

  /**
   * UMP's consent status.
   *
   * @since 0.1.0
   */
  status: ConsentStatus;

  /**
   * Whether the app must offer a way to reopen the privacy options form, for
   * example a row in its settings screen that calls `showPrivacyOptionsForm()`.
   *
   * @since 0.1.0
   */
  privacyOptionsRequired: boolean;

  /**
   * Whether GDPR applies to this user, as stored by UMP in `IABTCF_gdprApplies`.
   *
   * @since 0.1.0
   */
  gdprApplies: boolean;

  /**
   * The raw `IABTCF_PurposeConsents` string: character `n - 1` is `'1'` when
   * TCF purpose `n` is consented. Empty when there is none.
   *
   * @since 0.1.0
   */
  purposeConsents: string;

  /**
   * The answer translated to Consent Mode v2. Everything is `false` until UMP
   * knows whether this user has to be asked.
   *
   * @since 0.1.0
   */
  consentMode: ConsentModeSignals;

  /**
   * Message of the error that interrupted the request or the form, if any.
   * The rest of the state is still valid: it reflects what UMP has stored.
   *
   * @since 0.1.0
   */
  error?: string;
}

export interface UmpConsentPlugin {
  /**
   * Update the consent information and show the consent form if it is
   * required. Call it on every app launch, before initializing any ads SDK.
   *
   * Never rejects: when the request or the form fails it resolves with the
   * stored state and an `error` message.
   *
   * @since 0.1.0
   */
  requestConsent(options?: RequestConsentOptions): Promise<ConsentState>;

  /**
   * Show the privacy options form so the user can change their choice.
   * Offer it when `privacyOptionsRequired` is `true`. Never rejects.
   *
   * @since 0.1.0
   */
  showPrivacyOptionsForm(): Promise<ConsentState>;

  /**
   * The stored consent state, without any network request or UI.
   *
   * `gdprApplies`, `purposeConsents` and `consentMode` are read from storage
   * and are valid from the start. `status` and `canRequestAds` come from UMP,
   * which reports `'unknown'` and `false` until `requestConsent()` has been
   * called in the current launch.
   *
   * @since 0.1.0
   */
  getConsentState(): Promise<ConsentState>;

  /**
   * Forget the stored consent so the form shows again. For testing only.
   *
   * @since 0.1.0
   */
  reset(): Promise<void>;
}
