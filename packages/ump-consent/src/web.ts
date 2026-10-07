import { WebPlugin } from '@capacitor/core';

import type { ConsentState, UmpConsentPlugin } from './definitions';

const UNAVAILABLE = 'UMP consent is only available on Android.';

export class UmpConsentWeb extends WebPlugin implements UmpConsentPlugin {
  async requestConsent(): Promise<ConsentState> {
    throw this.unavailable(UNAVAILABLE);
  }

  async showPrivacyOptionsForm(): Promise<ConsentState> {
    throw this.unavailable(UNAVAILABLE);
  }

  async getConsentState(): Promise<ConsentState> {
    throw this.unavailable(UNAVAILABLE);
  }

  async reset(): Promise<void> {
    throw this.unavailable(UNAVAILABLE);
  }
}
