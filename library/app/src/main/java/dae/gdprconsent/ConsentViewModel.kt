package dae.gdprconsent

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import java.util.*

class ConsentViewModel(application: Application) : AndroidViewModel(application) {

    val consentRequests = ArrayList<ConsentRequest>()

    fun updateConsentRequest(request: ConsentRequest) {
        if (consentRequests.size > 0) {
            for (i in 0..consentRequests.size) {
                if (consentRequests[i].key == request.key) {
                    consentRequests[i] = request
                    break
                }
            }
        }
    }

    fun addConsentRequests(request: List<ConsentRequest>) {
        consentRequests.addAll(request)
    }
}
