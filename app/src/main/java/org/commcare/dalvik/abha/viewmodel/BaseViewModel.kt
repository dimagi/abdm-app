package org.commcare.dalvik.abha.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import org.commcare.dalvik.domain.model.AbdmErrorModel

abstract class BaseViewModel:ViewModel()

/**
 * UI State
 */
sealed class GenerateAbhaUiState {
    data class Loading(val isLoading: Boolean) : GenerateAbhaUiState()
    object TranslationReceived : GenerateAbhaUiState()
    object ValidState : GenerateAbhaUiState()
    object InvalidState : GenerateAbhaUiState()
    object MobileOtpRequested : GenerateAbhaUiState()
    object AadhaarOtpRequested : GenerateAbhaUiState()
    object AuthOtpRequested : GenerateAbhaUiState()
    object VerifyAuthOtpRequested : GenerateAbhaUiState()
    object VerifyMobileOtpRequested : GenerateAbhaUiState()
    object VerifyAadhaarOtpRequested : GenerateAbhaUiState()
    object AbhaAvailabilityRequested : GenerateAbhaUiState()

    object PatientHealthDataRequested : GenerateAbhaUiState()
    object Blocked : GenerateAbhaUiState()

    data class Success(val data: JsonObject, val requestType: RequestType) :
        GenerateAbhaUiState()

    data class Error(val data: JsonObject, val requestType: RequestType) : GenerateAbhaUiState()
    data class AbdmError(val data: AbdmErrorModel, val requestType: RequestType) :
        GenerateAbhaUiState()
}

/**
 * Request type sent
 */
enum class RequestType {
    MOBILE_OTP,
    MOBILE_OTP_VERIFY,

    AADHAAR_OTP,
    AADHAAR_OTP_VERIFY,

    AUTH_METHODS,

    GENERATE_AUTH_OTP,
    VERIFY_AUTH_OTP,

    CONFIRM_AUTH_AADHAAR_OTP,
    CONFIRM_AUTH_MOBILE_OTP,

    ABHA_AVAILABILITY,
    FETCH_ABHA_CARD,

    CREATE_PATIENT_CONSENT,
    FETCH_PATIENT_CONSENT,
    FETCH_CONSENT_ARTEFACTS

}