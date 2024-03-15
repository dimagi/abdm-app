package org.commcare.dalvik.domain.model

import android.hardware.SensorAdditionalInfo
import com.google.gson.annotations.SerializedName
import java.io.Serializable

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

// CARE CONTEXT AUTH FETCH AND INIT
data class CCAuthModesRequestModel(val id:String,
    val purpose:String = "LINK",
    val authMode: String?,
    val requester:CCRequesterModel){
}

data class CCRequesterModel(val type: String = "HIP", val id: String)

// CONFIRM AUTH API
data class ConfirmAuthModel(val transactionId:String,val credential: Credential)

data class Credential(var authCode:String? = null,var demographic: Demographic?  = null)

// CARE CONTEXT LINKING API
data class CCLinkModel(val accessToken:String,@SerializedName("hip_id") val hipId:String,val patient:CCPatientDetails,val healthId: String)

data class CCPatientDetails(val referenceNumber:String,val display:String,val careContexts:List<CCDetail>,val demographics:Demographic)


data class Demographic(val name:String ,var gender:String ,val dateOfBirth:String ,var phoneNumber :String?):Serializable

data class CCDetail(val referenceNumber:String,val display: String,val hiTypes:List<String>,val additionalInfo: AdditionalInfo)

data class AdditionalInfo(val domain:String,var record_date:String)


data class PatientNotificationModel(val phoneNo:String, val hip:HipModel)

data class HipModel(val id:String)




