package com.matchpulse.live.core.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor() {
    private var consentInformation: ConsentInformation? = null

    val canRequestAds: Boolean
        get() = consentInformation?.canRequestAds() ?: true

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation?.isPrivacyOptionsRequired() ?: false

    fun gatherConsent(activity: Activity, onComplete: (Exception?) -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation?.let { info ->
            if (info.isConsentFormAvailable()) {
                UserMessagingPlatform.loadConsentForm(
                    activity,
                    object : UserMessagingPlatform.ConsentFormLoadCallback() {
                        override fun onConsentFormLoaded(consentForm: ConsentForm) {
                            consentForm.show(
                                activity,
                                ConsentForm.OnConsentFormDismissedListener { formError ->
                                    onComplete(formError?.let { Exception(it.message) })
                                },
                            )
                        }

                        override fun onConsentFormLoadError(formError: FormError) {
                            Log.e(LOG_TAG, "Consent form load error: ${formError.message}")
                            onComplete(Exception(formError.message))
                        }
                    },
                )
            } else {
                onComplete(null)
            }
        } ?: onComplete(null)
    }

    fun showPrivacyOptionsForm(activity: Activity, onComplete: (Exception?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(
            activity,
            object : UserMessagingPlatform.OnConsentFormDismissedListener() {
                override fun onConsentFormDismissed(formError: FormError?) {
                    onComplete(formError?.let { Exception(it.message) })
                }
            },
        )
    }

    companion object {
        private const val LOG_TAG = "ConsentManager"
    }
}
