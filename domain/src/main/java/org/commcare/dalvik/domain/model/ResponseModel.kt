package org.commcare.dalvik.domain.model

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
}

class AbdmErrorDetail() {
    lateinit var message: String
    lateinit var code: String
    lateinit var attribute: Any
}

data class OtpResponseModel(val txnId: String)

data class AbhaVerificationResultModel(
    val status: String = "",
    var healthId: String,
    @SerializedName("user_token") var userToken: String? = null
) : Serializable

data class CheckAbhaResponseModel(
    @SerializedName("health_id") var healthId: String,
    var exists: Boolean
)

data class HealthCardResponseModel(@SerializedName("health_card") var healthCard: String)