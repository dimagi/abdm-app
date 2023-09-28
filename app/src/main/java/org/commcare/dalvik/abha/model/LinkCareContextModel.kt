package org.commcare.dalvik.abha.model

class LinkCareContextModel {
    lateinit var patientAbhaId: String
    lateinit var hipId: String
    lateinit var patient: PatientDetail

}

data class PatientDetail(var referenceNumber:String,var display:String,var careContexts:List<CareContextDetail>)

data class CareContextDetail(var referenceNumber: String,var display:String, var hiTypes:List<String>)