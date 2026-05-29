package com.matchpulse.live.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor() {

    private lateinit var consentInformation: ConsentInformation

    /**
     * Helper variable to check if ads can be requested.
     */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * Request consent information update.
     */
    fun gatherConsent(
        activity: Activity,
        onConsentGathered: (Error?) -> Unit
    ) {
        consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        val debugSettings = ConsentDebugSettings.Builder(activity)
            // Add test device IDs if needed for forced GDPR testing
            // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            // .addTestDeviceHashedId("...") 
            .build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(ADS_LOG_TAG, "Consent form error: ${formError.message}")
                    }
                    onConsentGathered(if (formError != null) Error(formError.message) else null)
                }
            },
            { requestError ->
                Log.w(ADS_LOG_TAG, "Consent request error: ${requestError.message}")
                onConsentGathered(Error(requestError.message))
            }
        )
    }

    /**
     * Returns true if the user has a requirement for a consent form.
     */
    fun isPrivacyOptionsRequired(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus == 
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /**
     * Show the privacy options form (e.g. from settings).
     */
    fun showPrivacyOptionsForm(activity: Activity, onComplete: (Error?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onComplete(if (formError != null) Error(formError.message) else null)
        }
    }
}
