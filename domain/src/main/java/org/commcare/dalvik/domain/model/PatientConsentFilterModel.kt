package org.commcare.dalvik.domain.model

class PatientConsentFilterModel(
    var abhaId: String){
    var filterText: String? = null
    var fromDate: String? = null
    var toDate: String? = null
}

