package dae.gdprtest;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import dae.gdprconsent.ConsentHelper;
import dae.gdprconsent.ConsentRequest;
import dae.gdprconsent.Constants;

public class MainJavaActivity extends AppCompatActivity {

    private final static int RC_CONSENT = 1;
    private final static int RC_RESTART = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ArrayList<ConsentRequest> list = new ArrayList<>();

        list.add(new ConsentRequest(
                "AGE_16",
                false,
                false,
                true,
                "2018-05-09",
                "How old are you?",
                "Age",
                "https://gdpr-info.eu/art-8-gdpr/",
                "You must be at least 16 years of age to use this App.",
                "Due to European regulations, you must be at least 16 years of age to use this App."
        ));

        list.add(new ConsentRequest(
                "BASIC_APP",
                false,
                false,
                true,
                "2018-05-09",
                "Basic functions",
                "Application",
                "http://example.com/gdpr",
                "We store your E-mail address, password, billing address, ..., on our servers.",
                "We need this information to perform the basic functionality of this App. Fill this section with more information about how each item is specific required."
        ));

        list.add(new ConsentRequest(
                "FIREBASE_STATISTICS",
                false,
                false,
                false,
                "2018-05-08",
                "Statistics",
                "Analytics",
                "https://firebase.google.com/support/privacy/",
                "Firebase stores your Mobile Ad ID, Android ID, Instance ID and Analytics App Instance IDs. ID specific data is deleted after 60 days.",
                "We send this information to Firebase in order that we can better understand our users, enabling us to create better Apps."
        ));

        list.add(new ConsentRequest(
                "FIREBASE_CRASH",
                false,
                false,
                false,
                "2018-05-07",
                "Crash reporting",
                "Analytics",
                "https://firebase.google.com/support/privacy",
                "Firebase stores your Instance ID and crash traces. Crash traces are deleted after 180 days.",
                "We need this to learn how and when crashes are happening in our App."
        ));

        if(ConsentHelper.hasNewOrRequired(this, list)) {
            ConsentHelper.showGdprOnlyNew(this, RC_CONSENT, list);
        }

        findViewById(R.id.goToGdpr).setOnClickListener(view -> ConsentHelper.showGdpr(this, RC_CONSENT, list));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_CONSENT) {
            if(resultCode == Activity.RESULT_OK) {
                // User passed through the consent system completely
                
                boolean consentChanged = data.getExtras() != null && data.getExtras().getBoolean(Constants.KEY_CONSENT_CHANGED);
				if(consentChanged) {
					Intent intent = new Intent(this, MainActivity.class);
					PendingIntent pending = PendingIntent.getActivity(this, RC_RESTART, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
					if (alarmManager != null) {
						alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pending);
					}
					finish();
					System.exit(0);
				}

                Log.v("CONSENT", "Consent for basic App functions: "+ConsentHelper.hasConsent("BASIC_APP"));
                Log.v("CONSENT", "Consent for statistics collection: "+ConsentHelper.hasConsent("FIREBASE_STATISTICS"));
                Log.v("CONSENT", "Consent for crash reporting: "+ConsentHelper.hasConsent("FIREBASE_CRASH"));
            } else {
                // User closed the consent system without progressing through it
                finish();
            }
        }
    }
}
