package com.byvik.capacitor.umpconsent;

import android.annotation.SuppressLint;
import android.content.Context;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus;
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType;
import java.util.EnumMap;

/**
 * The only class that touches Firebase. Keeping it apart lets the plugin load in apps that do not
 * ship Firebase Analytics: its types are resolved when apply() runs, not when the plugin loads.
 */
final class FirebaseConsentMode {

    private FirebaseConsentMode() {}

    // The permissions lint asks for come from Firebase's own manifest, which is present in every
    // app where this code can run at all.
    @SuppressLint("MissingPermission")
    static void apply(Context context, boolean analyticsStorage, boolean adStorage, boolean adUserData, boolean adPersonalization) {
        EnumMap<ConsentType, ConsentStatus> consent = new EnumMap<>(ConsentType.class);
        consent.put(ConsentType.ANALYTICS_STORAGE, status(analyticsStorage));
        consent.put(ConsentType.AD_STORAGE, status(adStorage));
        consent.put(ConsentType.AD_USER_DATA, status(adUserData));
        consent.put(ConsentType.AD_PERSONALIZATION, status(adPersonalization));
        FirebaseAnalytics.getInstance(context).setConsent(consent);
    }

    private static ConsentStatus status(boolean granted) {
        return granted ? ConsentStatus.GRANTED : ConsentStatus.DENIED;
    }
}
