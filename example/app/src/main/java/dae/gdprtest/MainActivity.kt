package dae.gdprtest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import dae.gdprconsent.ConsentHelper
import dae.gdprconsent.ConsentRequest
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val RC_CONSENT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = ArrayList<ConsentRequest>()

        list.add(ConsentRequest(
                key = "BASIC_APP",
                isRequired = true,
                added = "2018-05-09",
                title = "Basic functions",
                category = "Application",
                moreInformation = "http://example.com/gdpr",
                what = "We store your E-mail address, password, billing address, ..., on our servers.",
                whyNeeded = "We need this information to perform the basic functionality of this App. Fill this section with more information about how each item is specific required."
        ))

        list.add(ConsentRequest(
                key = "FIREBASE_STATISTICS",
                isRequired = false,
                added = "2018-05-08",
                title = "Statistics",
                category = "Analytics",
                moreInformation = "https://firebase.google.com/support/privacy/",
                what = "Firebase stores your Mobile Ad ID, Android ID, Instance ID and Analytics App Instance IDs. ID specific data is deleted after 60 days.",
                whyNeeded = "We send this information to Firebase in order that we can better understand our users, enabling us to create better Apps."
        ))

        list.add(ConsentRequest(
                key = "FIREBASE_CRASH",
                isRequired = false,
                added = "2018-05-07",
                title = "Crash reporting",
                category = "Analytics",
                moreInformation = "https://firebase.google.com/support/privacy",
                what = "Firebase stores your Instance ID and crash traces. Crash traces are deleted after 180 days.",
                whyNeeded = "We need this to learn how and when crashes are happening in our App."
        ))

        ConsentHelper.showGdprOnlyNew(this, RC_CONSENT, list)

        goToGdpr.setOnClickListener {
            ConsentHelper.showGdpr(this, RC_CONSENT, list)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_CONSENT) {
            if(resultCode == Activity.RESULT_OK) {
                // User passed through the consent system completely

                Log.v("CONSENT", "Consent for basic App functions: ${ConsentHelper.hasConsent("BASIC_APP")}")
                Log.v("CONSENT", "Consent for statistics collection: ${ConsentHelper.hasConsent("FIREBASE_STATISTICS")}")
                Log.v("CONSENT", "Consent for crash reporting: ${ConsentHelper.hasConsent("FIREBASE_CRASH")}")
            } else {
                // User closed the consent system without progressing through it
                finish()
            }
        }
    }
}
