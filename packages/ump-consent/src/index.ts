import { registerPlugin } from '@capacitor/core';

import type { UmpConsentPlugin } from './definitions';

const UmpConsent = registerPlugin<UmpConsentPlugin>('UmpConsent', {
  web: () => import('./web').then((m) => new m.UmpConsentWeb()),
});

export * from './definitions';
export { UmpConsent };
