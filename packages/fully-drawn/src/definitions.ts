export interface FullyDrawnPlugin {
  /**
   * Tell Android the app is fully drawn and usable. Call it once, when the
   * first real screen is on display.
   *
   * Only the first call of each launch is reported; later calls do nothing.
   * It never rejects, and on iOS and web it is a no-op, so it is safe to call
   * unconditionally.
   *
   * @since 0.1.0
   */
  reportFullyDrawn(): Promise<void>;
}
