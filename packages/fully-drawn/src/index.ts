import { registerPlugin } from '@capacitor/core';

import type { FullyDrawnPlugin } from './definitions';

const noop = () => import('./web').then((m) => new m.FullyDrawnWeb());

// iOS has no native side: registering the no-op there keeps the call from
// rejecting as unimplemented.
const FullyDrawn = registerPlugin<FullyDrawnPlugin>('FullyDrawn', {
  web: noop,
  ios: noop,
});

export * from './definitions';
export { FullyDrawn };
