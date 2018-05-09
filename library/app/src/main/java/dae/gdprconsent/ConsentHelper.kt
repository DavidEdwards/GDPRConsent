package dae.gdprconsent

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Helper to interact with user consent.
 *
 * This helper contains a persistent cache containing keys and state of consent.
 */
object ConsentHelper {
    /**
     * Consent key -> isConsented cache
     */
    @JvmStatic
    val consentCache: HashMap<String, Boolean> = HashMap()

    /**
     * Fill the cache with key and consent data.
     * This is for the [hasConsent] method.
     */
    internal fun populate(list: List<ConsentRequest>) {
        for (request in list) {
            consentCache[request.key] = request.isConsented
        }
    }

    /**
     * If you require consent data before the first run of the [ConsentActivity] (as called
     * from [showGdpr], [showGdprOnlyNew] and [showGdprIfRequest]), then you will need to
     * populate the consent cache with this method.
     */
    @JvmStatic
    fun populate(context: Context) {
        val prefs = context.getSharedPreferences(Constants.PREF_GDPR, Context.MODE_PRIVATE)
        for(( key, _ ) in prefs.all) {
            if(key.startsWith("gdpr.")) {
                val request = ConsentRequest(key.substring(5))
                request.load(prefs)
                consentCache[request.key] = request.isConsented
            }
        }
    }

    /**
     * Test whether a specific key has user consent.
     * Checks are made with [consentCache]. This cache is populated after the [ConsentActivity] is
     * run. Even if it immediately closes. You can also immediately populate the cache with [populate].
     */
    @JvmStatic
    fun hasConsent(key: String): Boolean {
        return consentCache[key] ?: false
    }

    /**
     * Show the GDPR dialog.
     *
     * This will show in all cases, showing all consent items.
     *
     * Call this if you have a GDPR button in your App. In settings for example. This will give the
     * user an opportunity to give or revoke their consent.
     */
    @JvmStatic
    fun showGdpr(activity: Activity, requestCode: Int, consentItems: ArrayList<ConsentRequest>) {
        val intent = Intent(activity, ConsentActivity::class.java)
        intent.putParcelableArrayListExtra(Constants.CONSENT_REQUESTS, consentItems)
        intent.putExtra(Constants.KEY_MODE, Constants.MODE_ALWAYS_SHOW)
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * Show the GDPR dialog.
     *
     * This will show only if there is a consent item that is required that is not isConsented to.
     */
    @JvmStatic
    fun showGdprIfRequest(activity: Activity, requestCode: Int, consentItems: ArrayList<ConsentRequest>) {
        val intent = Intent(activity, ConsentActivity::class.java)
        intent.putParcelableArrayListExtra(Constants.CONSENT_REQUESTS, consentItems)
        intent.putExtra(Constants.KEY_MODE, Constants.MODE_SHOW_IF_REQUIRED)
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * Show the GDPR dialog.
     *
     * This will only show if there are new consent items that are unseen.
     *
     * Call this before showing any part of your UI or enabling any services or APIs. Services that
     * start with your App should be specifically set disabled until this dialog is completed. At
     * which time you can use the user consent information to determine if a service should start.
     */
    @JvmStatic
    fun showGdprOnlyNew(activity: Activity, requestCode: Int, consentItems: ArrayList<ConsentRequest>) {
        val intent = Intent(activity, ConsentActivity::class.java)
        intent.putParcelableArrayListExtra(Constants.CONSENT_REQUESTS, consentItems)
        intent.putExtra(Constants.KEY_MODE, Constants.MODE_SHOW_NOT_SEEN)
        activity.startActivityForResult(intent, requestCode)
    }
}