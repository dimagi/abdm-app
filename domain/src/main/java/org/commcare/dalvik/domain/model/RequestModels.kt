package org.commcare.dalvik.domain.model

import com.google.gson.annotations.SerializedName

abstract class BaseModel

data class AadhaarOtpRequestModel(@SerializedName("aadhaar") val aadhaarNumber: String) :
    BaseModel()

data class MobileOtpRequestModel(
    @SerializedName("mobile_number") val mobileNUmber: String,
    val txn_id: String
) : BaseModel()

data class VerifyOtpRequestModel(val txn_id: String, val otp: String,@SerializedName("health_id") val healthId: String? = null)

data class GenerateAuthOtpModel(
    @SerializedName("health_id") val healthId: String,
    @SerializedName("auth_method") val authMethod: String
)

data class GetAuthMethodRequestModel( @SerializedName("health_id") val healthId: String)

data class AbhaCardRequestModel(@SerializedName("user_token") val userToken: String)
data class AbhaVerificationRequestModel(@SerializedName("health_id") val healthId: String)