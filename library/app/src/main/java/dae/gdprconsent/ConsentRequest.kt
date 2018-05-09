package dae.gdprconsent

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

/**
 * A data class for user consent. It represents the request given to a user and the state thereafter.
 *
 * Once it has been created once, the language is immutable. Only [isConsented] and [isSeen] can change.
 * If you need to change the language of this request, you will need to make a new [key] and stop using
 * the original!
 *
 * @param key The key is used to identify this consent. It is used in [ConsentHelper.hasConsent] to test for consent.
 * @param isConsented True if the user has given consent for this request.
 * @param isSeen True if the user has seen this consent request (even if they haven't consented to it).
 * @param isRequired True if the user must consent to this request to use the App.
 * @param added The date and / or time that this consent request was added. This is fluff information.
 *              Useful for the user to know when a particular request was added to the App. Do not
 *              generate this dynamically!
 * @param title The header text for this request. This should be *obvious* to the user as to the purpose of the request.
 * @param category The type of request. Examples are "Application" for critical functions or
 *                 "Statistics" for analytics services. This is freeform.
 * @param what What does your App / external service use / store. Be specific and to the point.
 *             GDPR requires plain language to be used to be understandable.
 * @param whyNeeded Why does your App / external service need this data. Be specific and to the point.
 *                  GDPR requires plain language to be used to be understandable.
 * @param moreInformation Insert a URL here to link to external content.
 */
data class ConsentRequest(
        var key: String,
        var isConsented: Boolean = false,
        var isSeen: Boolean = false,
        var isRequired: Boolean = false,
        var added: String = "",
        var title: String = "",
        var category: String = "",
        var what: String = "",
        var whyNeeded: String = "",
        var moreInformation: String = ""
) : Parcelable {

    protected constructor(source: Parcel) : this(
        key = source.readString(),
        isConsented = source.readByte().toInt() != 0,
        isSeen = source.readByte().toInt() != 0,
        isRequired = source.readByte().toInt() != 0,
        added = source.readString(),
        title = source.readString(),
        category = source.readString(),
        what = source.readString(),
        whyNeeded = source.readString(),
        moreInformation = source.readString()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(key)
        dest.writeByte((if (isConsented) 1 else 0).toByte())
        dest.writeByte((if (isSeen) 1 else 0).toByte())
        dest.writeByte((if (isRequired) 1 else 0).toByte())
        dest.writeString(added)
        dest.writeString(title)
        dest.writeString(category)
        dest.writeString(what)
        dest.writeString(whyNeeded)
        dest.writeString(moreInformation)
    }

    override fun describeContents(): Int {
        return 0
    }


    /**
     * Turn this consent request into a JSON string for saving to [SharedPreferences][android.content.SharedPreferences].
     */
    private fun asJson(): String {
        try {
            val o = JSONObject()
            o.put(Constants.PREF_KEY_CONSENTED, isConsented)
            o.put(Constants.PREF_KEY_SEEN, isSeen)
            o.put(Constants.PREF_KEY_REQUIRED, isRequired)
            o.put(Constants.PREF_KEY_ADDED, added)
            o.put(Constants.PREF_KEY_TITLE, title)
            o.put(Constants.PREF_KEY_CATEGORY, category)
            o.put(Constants.PREF_KEY_DESCRIPTION, what)
            o.put(Constants.PREF_KEY_WHY_NEEDED, whyNeeded)
            o.put(Constants.PREF_KEY_MORE_INFORMATION, moreInformation)

            return o.toString()
        } catch (e: Exception) {
            return "{}"
        }
    }

    /**
     * Populate this consent request with data from a JSON.
     */
    private fun fromJson(json: String) {
        try {
            val o = JSONObject(json)
            isConsented = o.optBoolean(Constants.PREF_KEY_CONSENTED, false)
            isSeen = o.optBoolean(Constants.PREF_KEY_SEEN, false)
            isRequired = o.getBoolean(Constants.PREF_KEY_REQUIRED)
            added = o.getString(Constants.PREF_KEY_ADDED)
            title = o.getString(Constants.PREF_KEY_TITLE)
            category = o.getString(Constants.PREF_KEY_CATEGORY)
            what = o.getString(Constants.PREF_KEY_DESCRIPTION)
            whyNeeded = o.getString(Constants.PREF_KEY_WHY_NEEDED)
            moreInformation = o.getString(Constants.PREF_KEY_MORE_INFORMATION)
        } catch (e: Exception) {
        }

    }

    /**
     * Save this consent request to [SharedPreferences][android.content.SharedPreferences].
     * This is how the state of [isConsented] and [isSeen] is saved.
     * It also guarantees immutability for a given consents language.
     */
    internal fun save(context: Context): ConsentRequest {
        val prefs = context.getSharedPreferences(Constants.PREF_GDPR, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("gdpr.$key", asJson())
        editor.apply()

        return this
    }

    /**
     * Load this consent request from [SharedPreferences][android.content.SharedPreferences].
     * This reloads original language and the state of [isConsented] and [isSeen].
     */
    internal fun load(context: Context): ConsentRequest {
        val prefs = context.getSharedPreferences(Constants.PREF_GDPR, Context.MODE_PRIVATE)
        return load(prefs)
    }

    /**
     * Load this consent request from [SharedPreferences][android.content.SharedPreferences].
     * This reloads original language and the state of [isConsented] and [isSeen].
     */
    internal fun load(prefs: SharedPreferences): ConsentRequest {
        if (prefs.contains("gdpr.$key")) {
            fromJson(prefs.getString("gdpr.$key", "{}"))
        }

        return this
    }

    companion object CREATOR : Parcelable.Creator<ConsentRequest> {
        override fun createFromParcel(parcel: Parcel): ConsentRequest {
            return ConsentRequest(parcel)
        }

        override fun newArray(size: Int): Array<ConsentRequest?> {
            return arrayOfNulls(size)
        }
    }
}
