package org.commcare.dalvik.domain.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class AbdmErrorModel() {
    lateinit var code: String
    lateinit var message: String
    lateinit var details: List<AbdmErrorDetail>

    fun getActualMessage(): String {
        return details[0].message
    }

    fun getAbdmErrorCode(): String {
        return details[0].code
    }

    fun getErrorMsg(): String {
        return getActualMessage().ifEmpty {
            message
        }
    }
}

class AbdmErrorDetail {
    var message: String = ""
    var code: String = ""
    lateinit var attribute: Any
}

data class OtpResponseModel(val txnId: String)

data class AbhaVerificationResultModel(
    val status: String = "",
    var healthIdNumber: String?,
    var healthId: String,
    @SerializedName("user_token") var userToken: String? = null,
    var aadharData: JsonObject

) : Serializable{

    fun putAadharData(responseJsonObject: JsonObject){
        aadharData = responseJsonObject
        try {
            aadharData.remove("user_token")
            aadharData.remove("status")
            aadharData.remove("txnId")
            aadharData.remove("authMethods")
        }catch (e:Exception){
            //
        }
    }
}

data class CheckAbhaResponseModel(
    @SerializedName("health_id") var healthId: String,
    var exists: Boolean
)

data class HealthCardResponseModel(@SerializedName("health_card") var healthCard: String)
data class NotifyPatientResponseModel(var status: Boolean)


data class PatientHealthDataModel(
    @SerializedName("transaction_id") var transactionId: String,
    @SerializedName("page_count") var pageCount: Int,
    var page: Int,
    var next: String?
) {
    lateinit var results: MutableList<HealthContentModel>
}

class HealthContentModel {
    @SerializedName("care_context_reference")
    lateinit var careContextRef: String
    lateinit var title: String
    lateinit var content: MutableList<SectionContent>

}

class SectionContent {
    lateinit var section: String
    lateinit var resource: String
    lateinit var entries: MutableList<SectionEntry>
}

class SectionEntry {
    lateinit var label: String
    lateinit var value: String
}


class CCGenerateOtpResponseModel {
    lateinit var transactionId: String
}

class CCVerifyOtpResponseModel {
    lateinit var accessToken: String
}

class CCLinkSuccessResponseModel {
    lateinit var status: String
}

