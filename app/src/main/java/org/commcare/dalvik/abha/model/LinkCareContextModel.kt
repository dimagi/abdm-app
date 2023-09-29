package org.commcare.dalvik.abha.model

import org.commcare.dalvik.domain.model.CCAuthModesRequestModel
import org.commcare.dalvik.domain.model.CCLinkModel
import org.commcare.dalvik.domain.model.CCPatientDetails
import org.commcare.dalvik.domain.model.CCRequesterModel

class LinkCareContextModel {
    lateinit var patientAbhaId: String
    lateinit var hipId: String
    lateinit var purpose: String
    lateinit var patient: CCPatientDetails


    fun getAuthModesRequestModel(authMode:String? = null) = try {
        CCAuthModesRequestModel(patientAbhaId, purpose, authMode, CCRequesterModel(id = hipId))
    } catch (e: Exception) {
        null
    }

    fun getCCLinkRequestModel(accessToken:String):CCLinkModel{
        return CCLinkModel(accessToken,hipId,patient)
    }

}

