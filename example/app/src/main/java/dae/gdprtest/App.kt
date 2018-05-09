package dae.gdprtest

import android.app.Application
import android.util.Log
import dae.gdprconsent.ConsentHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Consent data is only fetched once the GDPR dialog has finished...
        Log.v("CONSENT", "Before Populating: Consent for basic App functions: ${ConsentHelper.hasConsent("BASIC_APP")}")

        // ... or you manually populate the data!
        ConsentHelper.populate(this)

        // Now we have the consent available!
        Log.v("CONSENT", "After Populating: Consent for basic App functions: ${ConsentHelper.hasConsent("BASIC_APP")}")
    }
}