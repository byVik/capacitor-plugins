package com.byvik.capacitor.fullydrawn;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Lets the web app say when it is really ready.
 *
 * <p>Android vitals times a cold start up to the first frame the activity draws, and in a
 * Capacitor app that frame is an empty WebView. Activity.reportFullyDrawn() is the official way
 * to mark the moment the content is actually usable, and only the web app knows when that is.
 */
@CapacitorPlugin(name = "FullyDrawn")
public class FullyDrawnPlugin extends Plugin {

    private boolean reported = false;

    // Reports once and never fails: this is a measurement, and a measurement must not be the
    // reason somebody does not reach the first screen.
    @PluginMethod
    public void reportFullyDrawn(PluginCall call) {
        if (!reported) {
            reported = true;
            try {
                getActivity().runOnUiThread(() -> {
                    try {
                        getActivity().reportFullyDrawn();
                    } catch (Throwable ignored) {
                        // Nothing to do: the launch simply goes unreported.
                    }
                });
            } catch (Throwable ignored) {
                // Same as above.
            }
        }
        call.resolve();
    }
}
