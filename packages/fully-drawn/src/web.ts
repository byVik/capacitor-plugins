import { WebPlugin } from '@capacitor/core';

import type { FullyDrawnPlugin } from './definitions';

export class FullyDrawnWeb extends WebPlugin implements FullyDrawnPlugin {
  async reportFullyDrawn(): Promise<void> {
    // There is nothing to report to outside Android.
  }
}
