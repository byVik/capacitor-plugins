package com.byvik.capacitor.umpconsent;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Google User Messaging Platform (UMP) consent, plus the translation of the answer into the four
 * Consent Mode v2 signals.
 *
 * <p>Showing the form is only half of the job: UMP stores the answer in TCF format, and without
 * the second half nothing tells Firebase to stop measuring a player who said no.
 *
 * <p>No method here rejects. Every path (network error, form that fails to load) resolves with
 * the current state, so a consent failure can never keep an app from starting.
 */
@CapacitorPlugin(name = "UmpConsent")
public class UmpConsentPlugin extends Plugin {

    // IAB TCF v2 keys. They are an IAB standard, not a Google one: every certified CMP, UMP
    // included, writes them to the app's default SharedPreferences.
    private static final String TCF_PURPOSE_CONSENTS = "IABTCF_PurposeConsents";
    private static final String TCF_GDPR_APPLIES = "IABTCF_gdprApplies";

    private ConsentInformation consentInformation;

    private ConsentInformation info() {
        if (consentInformation == null) {
            consentInformation = UserMessagingPlatform.getConsentInformation(getContext());
        }
        return consentInformation;
    }

    @PluginMethod
    public void requestConsent(PluginCall call) {
        final Activity activity = getActivity();
        if (activity == null) {
            call.resolve(state("Activity not available"));
            return;
        }

        ConsentRequestParameters.Builder params = new ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(
            Boolean.TRUE.equals(call.getBoolean("tagForUnderAgeOfConsent", false))
        );

        // Debug settings make a test device behave as if it were somewhere else. Without them the
        // EEA flow cannot be tried from outside the EEA, and the bug shows up with real users.
        String geography = call.getString("debugGeography");
        JSArray testDeviceIds = call.getArray("testDeviceIds");
        if (geography != null || testDeviceIds != null) {
            ConsentDebugSettings.Builder debug = new ConsentDebugSettings.Builder(getContext());
            if (geography != null) {
                debug.setDebugGeography(debugGeography(geography));
            }
            if (testDeviceIds != null) {
                for (int i = 0; i < testDeviceIds.length(); i++) {
                    String id = testDeviceIds.optString(i, null);
                    if (id != null) {
                        debug.addTestDeviceHashedId(id);
                    }
                }
            }
            params.setConsentDebugSettings(debug.build());
        }

        try {
            info().requestConsentInfoUpdate(
                activity,
                params.build(),
                () ->
                    activity.runOnUiThread(() ->
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, (formError) -> finish(call, formError))
                    ),
                (requestError) -> finish(call, requestError)
            );
        } catch (Throwable t) {
            call.resolve(state(String.valueOf(t.getMessage())));
        }
    }

    @PluginMethod
    public void showPrivacyOptionsForm(PluginCall call) {
        final Activity activity = getActivity();
        if (activity == null) {
            call.resolve(state("Activity not available"));
            return;
        }
        activity.runOnUiThread(() -> UserMessagingPlatform.showPrivacyOptionsForm(activity, (formError) -> finish(call, formError)));
    }

    @PluginMethod
    public void getConsentState(PluginCall call) {
        call.resolve(state(null));
    }

    @PluginMethod
    public void reset(PluginCall call) {
        try {
            info().reset();
        } catch (Throwable ignored) {
            // Nothing to reset.
        }
        call.resolve();
    }

    // A failed form grants nothing by itself: what counts is whatever UMP has stored.
    private void finish(PluginCall call, FormError error) {
        applyConsentMode();
        call.resolve(state(error == null ? null : error.getMessage()));
    }

    @ConsentDebugSettings.DebugGeography
    private static int debugGeography(String value) {
        switch (value) {
            case "eea":
                return ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA;
            case "regulatedUsState":
                return ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_REGULATED_US_STATE;
            case "other":
                return ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_OTHER;
            default:
                return ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED;
        }
    }

    // ---------- TCF -> Consent Mode ----------

    private static final class Signals {

        // False until UMP has said whether this user needs to be asked. While it is false nothing
        // is granted and nothing is applied, so the app's manifest defaults stay in force.
        boolean known;
        boolean gdprApplies;
        String purposeConsents = "";
        boolean analyticsStorage;
        boolean adStorage;
        boolean adUserData;
        boolean adPersonalization;
    }

    private SharedPreferences defaultPreferences() {
        Context context = getContext();
        return context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
    }

    private int consentStatus() {
        try {
            return info().getConsentStatus();
        } catch (Throwable t) {
            return ConsentInformation.ConsentStatus.UNKNOWN;
        }
    }

    // The TCF string is positional: the character at n - 1 is '1' when purpose n is consented.
    private static boolean purpose(String purposeConsents, int n) {
        return purposeConsents.length() >= n && purposeConsents.charAt(n - 1) == '1';
    }

    // The mapping Google documents for TCF, plus analytics_storage, which TCF does not cover and
    // is tied here to purpose 1 (store and/or access information on a device):
    //
    //   analytics_storage   <- purpose 1
    //   ad_storage          <- purpose 1
    //   ad_user_data        <- purposes 1 and 7
    //   ad_personalization  <- purposes 3 and 4
    //
    // Where GDPR does not apply everything is granted. That is the correct reading of "no need to
    // ask here": apps that default to denied in the manifest would otherwise never measure anyone
    // outside the regulated regions.
    private Signals signals() {
        Signals s = new Signals();
        SharedPreferences prefs = defaultPreferences();
        int status = consentStatus();
        boolean hasGdprKey = prefs.contains(TCF_GDPR_APPLIES);

        s.known =
            hasGdprKey || status == ConsentInformation.ConsentStatus.NOT_REQUIRED || status == ConsentInformation.ConsentStatus.OBTAINED;
        if (!s.known) {
            return s;
        }

        try {
            s.gdprApplies = hasGdprKey && prefs.getInt(TCF_GDPR_APPLIES, 0) == 1;
            String purposes = prefs.getString(TCF_PURPOSE_CONSENTS, "");
            s.purposeConsents = purposes == null ? "" : purposes;
        } catch (ClassCastException e) {
            // Unreadable preferences are treated as "GDPR applies, nothing consented".
            s.gdprApplies = true;
        }

        if (!s.gdprApplies) {
            s.analyticsStorage = true;
            s.adStorage = true;
            s.adUserData = true;
            s.adPersonalization = true;
        } else {
            boolean p1 = purpose(s.purposeConsents, 1);
            s.analyticsStorage = p1;
            s.adStorage = p1;
            s.adUserData = p1 && purpose(s.purposeConsents, 7);
            s.adPersonalization = purpose(s.purposeConsents, 3) && purpose(s.purposeConsents, 4);
        }
        return s;
    }

    private void applyConsentMode() {
        if (!getConfig().getBoolean("firebaseConsentMode", true)) {
            return;
        }
        Signals s = signals();
        if (!s.known) {
            return;
        }
        try {
            FirebaseConsentMode.apply(getContext(), s.analyticsStorage, s.adStorage, s.adUserData, s.adPersonalization);
        } catch (Throwable t) {
            // Firebase Analytics is not part of this app, or refused the update. Either way the
            // manifest defaults stay in force: measuring too little is recoverable, too much is not.
        }
    }

    private JSObject state(String error) {
        Signals s = signals();
        ConsentInformation info = null;
        try {
            info = info();
        } catch (Throwable ignored) {
            // Reported below as the most restrictive state.
        }

        boolean canRequestAds = false;
        boolean privacyOptionsRequired = false;
        if (info != null) {
            try {
                canRequestAds = info.canRequestAds();
                privacyOptionsRequired =
                    info.getPrivacyOptionsRequirementStatus() == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
            } catch (Throwable ignored) {
                // Keep the restrictive defaults.
            }
        }

        JSObject consentMode = new JSObject();
        consentMode.put("analyticsStorage", s.analyticsStorage);
        consentMode.put("adStorage", s.adStorage);
        consentMode.put("adUserData", s.adUserData);
        consentMode.put("adPersonalization", s.adPersonalization);

        JSObject ret = new JSObject();
        ret.put("canRequestAds", canRequestAds);
        ret.put("status", statusName(consentStatus()));
        ret.put("privacyOptionsRequired", privacyOptionsRequired);
        ret.put("gdprApplies", s.gdprApplies);
        ret.put("purposeConsents", s.purposeConsents);
        ret.put("consentMode", consentMode);
        if (error != null) {
            ret.put("error", error);
        }
        return ret;
    }

    private static String statusName(int status) {
        switch (status) {
            case ConsentInformation.ConsentStatus.NOT_REQUIRED:
                return "notRequired";
            case ConsentInformation.ConsentStatus.REQUIRED:
                return "required";
            case ConsentInformation.ConsentStatus.OBTAINED:
                return "obtained";
            default:
                return "unknown";
        }
    }
}
