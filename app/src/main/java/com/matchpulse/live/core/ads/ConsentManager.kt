package com.matchpulse.live.core.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor() {
    private var consentForm: ConsentForm? = null
    private var consentInformation: ConsentInformation? = null

    val canRequestAds: Boolean get() = consentInformation?.canRequestAds() ?: true
    val isPrivacyOptionsRequired: Boolean get() = consentInformation?.isPrivacyOptionsRequired() ?: false

    fun gatherConsent(activity: Activity, onComplete: (Exception?) -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation?.let { info ->
            if (info.isConsentFormAvailable()) {
                UserMessagingPlatform.loadConsentForm(activity) { form, loadError ->
                    if (loadError != null) {
                        Log.e(LOG_TAG, "Consent form load error", loadError)
                        onComplete(loadError)
                        return@loadConsentForm
                    }
                    consentForm = form
                    form?.show(activity) { formError ->
                        if (formError != null) Log.e(LOG_TAG, "Consent form show error", formError)
                        onComplete(formError)
                    }
                }
            } else {
                onComplete(null)
            }
        } ?: onComplete(null)
    }

    fun showPrivacyOptionsForm(activity: Activity, onComplete: (Exception?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onComplete)
    }

    companion object {
        private const val LOG_TAG = "ConsentManager"
    }
}
