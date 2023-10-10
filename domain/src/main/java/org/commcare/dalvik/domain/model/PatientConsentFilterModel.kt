package org.commcare.dalvik.domain.model

class PatientConsentFilterModel(
    var abhaId: String){
    var filterText: String? = null
    var fromDate: String? = null
    var toDate: String? = null
}


class ConsentArtefactFilterModel(var consentRequestId: String){
    var filterText: String? = null
}



