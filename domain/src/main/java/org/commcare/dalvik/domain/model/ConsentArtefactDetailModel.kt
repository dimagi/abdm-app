package org.commcare.dalvik.domain.model


class ConsentArtefactDetailModel {
    lateinit var hip: IdNameModel
    lateinit var hiu: IdNameModel
    lateinit var hiTypes: List<String>
    lateinit var patient:Patient
    lateinit var consentId:String
    lateinit var createdAt:String
    lateinit var schemaVersion:String
    lateinit var purpose: Purpose
    lateinit var requester: Requester
    lateinit var permission: ConsentPermission
    lateinit var consentManager: ConsentManager
    lateinit var careContexts: List<List<CareContext>>
}

data class ConsentManager(val id:String)
data class CareContext(val patientReference:String? , val careContextReference:String)


