package org.commcare.dalvik.abha.viewmodel

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.commcare.dalvik.abha.model.AbhaRequestModel
import org.commcare.dalvik.abha.utility.AppConstants
import org.commcare.dalvik.abha.utility.PropMutableLiveData
import org.commcare.dalvik.data.util.PrefKeys
import org.commcare.dalvik.domain.model.*
import org.commcare.dalvik.domain.usecases.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AbdmViewModel @Inject constructor(
    private val generateAuthOtpUsecase: GenerateAuthOtpUsecase,
    private val authenticationMethodsUsecase: GetAuthenticationMethodsUsecase,
    private val reqAadhaarOtpUsecase: RequestAadhaarOtpUsecase,
    private val reqMobileOtpUseCase: RequestMobileOtpUseCase,
    val saveDataUsecase: SaveDataUsecase,
    private val translationUseCase: GetTranslationUseCase,
    private val verifyAadhaarOtpUseCase: VerifyAadhaarOtpUseCase,
    private val verifyMobileOtpUseCase: VerifyMobileOtpUseCase,
    private val confirmAadhaarOtpUsecase: ConfirmAadhaarOtpUsecase,
    private val confirmMobileOtpUsecase: ConfirmMobileOtpUsecase,
    private val abhaAvailbilityUsecase: AbhaAvailabilityUsecase,
    private val fetchAbhaCardUseCase: FetchAbhaCardUseCase
) : BaseViewModel() {
    var selectedAuthMethod: String? = null
    var checkAbhaResponseModel:MutableLiveData<CheckAbhaResponseModel> = MutableLiveData()
    var abhaRequestModel: PropMutableLiveData<AbhaRequestModel> = PropMutableLiveData()
    val abhaDetailModel: MutableLiveData<AbhaDetailModel> = MutableLiveData()
    val uiState = MutableStateFlow<GenerateAbhaUiState>(GenerateAbhaUiState.InvalidState)
    val otpRequestBlocked: MutableLiveData<OtpRequestCallModel> = MutableLiveData()

    fun init(reqModel: AbhaRequestModel) {
        abhaRequestModel.setValue(reqModel)
    }

    fun resetUiState() {
        viewModelScope.launch {
            uiState.emit(GenerateAbhaUiState.Loading(false))
        }
    }

    fun validateData() {
        viewModelScope.launch {
            var isMobileNumberValid = false
            abhaRequestModel.value?.mobileNumber?.apply {
                if (this.isNotEmpty() && this.length == AppConstants.MOBILE_NUMBER_LENGTH) {
                    val firstChar = this.first().toString().toInt()
                    if (firstChar in IntRange(6, 9)) {
                        isMobileNumberValid = true
                    }
                }
            }

            var isAadhaarValid = false
            abhaRequestModel.value?.aadhaar?.apply {
                if (this.isNotEmpty() && this.length == AppConstants.AADHAR_NUMBER_LENGTH) {
                    isAadhaarValid = true
                }
            }

            uiState.emit(if (isMobileNumberValid && isAadhaarValid) GenerateAbhaUiState.ValidState else GenerateAbhaUiState.InvalidState)
        }
    }

    //--------------------- REQUEST_OTP
    /**
     * Req AadhaarOtp
     */
    fun requestAadhaarOtp() {
        viewModelScope.launch {
            //save otp req call count
            abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
                saveOtpRequestCallCount(aadhaarKey)
            }

            val aadhaarOtpFlow = reqAadhaarOtpUsecase.execute(abhaRequestModel.value!!.aadhaar)
            aadhaarOtpFlow.collect {
                when (it) {
                    is HqResponseModel.Loading -> {
                        Timber.d("EMIT Sending -> GenerateAbhaUiState.AadhaarOtpRequested")
                        uiState.emit(GenerateAbhaUiState.AadhaarOtpRequested)
                    }
                    is HqResponseModel.Success -> {
                        Timber.d("EMIT Sending -> GenerateAbhaUiState.Success")
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.AADHAAR_OTP
                            )
                        )
                    }
                    is HqResponseModel.Error -> {
                        Timber.d("EMIT Sending -> GenerateAbhaUiState.Error")
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.AADHAAR_OTP
                            )
                        )
                    }
                    is HqResponseModel.AbdmError -> {
                        Timber.d("EMIT Sending -> GenerateAbhaUiState.AbdmError")
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                it.value,
                                RequestType.AADHAAR_OTP
                            )
                        )
                    }
                }
            }
        }

    }

    /**
     * Request Mobile Otp
     */
    fun requestMobileOtp() {
        viewModelScope.launch {
            //save otp req call count
            abhaRequestModel.value?.aadhaar?.let {
                saveOtpRequestCallCount(it)
            }

            reqMobileOtpUseCase.execute(
                abhaRequestModel.value!!.mobileNumber,
                abhaRequestModel.value!!.txnId
            ).collect {
                when (it) {

                    is HqResponseModel.Loading -> {
                        Timber.d("EMIT Sending -> GenerateAbhaUiState.MobileOtpRequested")
                        uiState.emit(GenerateAbhaUiState.MobileOtpRequested)
                    }

                    is HqResponseModel.Success -> {
                        //save otp req call count
                        abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
                            saveOtpRequestCallCount(aadhaarKey)
                        }

                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.MOBILE_OTP
                            )
                        )
                    }

                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.MOBILE_OTP
                            )
                        )
                    }

                    is HqResponseModel.AbdmError ->{
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                it.value,
                                RequestType.MOBILE_OTP
                            )
                        )
                    }

                }
            }
        }
    }

    /**
     * Generate Auth OTP
     */
    fun getAuthOtp(healthId: String, authMethod: String) {
        viewModelScope.launch {
            //save otp req call count
            abhaRequestModel.value?.abhaId?.let { abhaIdKey ->
                saveOtpRequestCallCount(abhaIdKey)
            }

            generateAuthOtpUsecase.execute(healthId, authMethod).collect {
                when (it) {
                    HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.AuthOtpRequested)
                    }

                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.GENERATE_AUTH_OTP
                            )
                        )
                    }
                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.GENERATE_AUTH_OTP
                            )
                        )
                    }
                    is HqResponseModel.AbdmError -> {
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                it.value,
                                RequestType.GENERATE_AUTH_OTP
                            )
                        )

                    }
                }
            }
            saveData(PrefKeys.OTP_BLOCKED_TS.getKey(), System.currentTimeMillis().toString())
        }
    }

    //-------------------------- VERIFY OTP

    /**
     * Verify Mobile OTP
     */
    fun verifyMobileOtp(verifyMobileOtpRequestModel: VerifyOtpRequestModel) {
        viewModelScope.launch(Dispatchers.Main) {
            verifyMobileOtpUseCase.execute(verifyMobileOtpRequestModel).collect {
                when (it) {
                    HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.VerifyMobileOtpRequested)
                    }

                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.MOBILE_OTP_VERIFY
                            )
                        )

                    }
                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.MOBILE_OTP_VERIFY
                            )
                        )
                    }
                    is HqResponseModel.AbdmError -> {
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                it.value,
                                RequestType.MOBILE_OTP_VERIFY
                            )
                        )
                    }

                }
            }
        }
    }

    /**
     * Verify Aadhaar OTP
     */
    fun verifyAadhaarOtp(verifyOtpRequestModel: VerifyOtpRequestModel) {
        viewModelScope.launch(Dispatchers.Main) {
            verifyAadhaarOtpUseCase.execute(verifyOtpRequestModel).collect {
                when (it) {
                    HqResponseModel.Loading -> {
                        Timber.d("EMIT Sending -> GenerateAbhaUiState.VerifyAadhaarOtpRequested")
                        uiState.emit(GenerateAbhaUiState.VerifyAadhaarOtpRequested)
                    }

                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.AADHAAR_OTP_VERIFY
                            )
                        )

                    }
                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.AADHAAR_OTP_VERIFY
                            )
                        )
                    }
                    is HqResponseModel.AbdmError -> {
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                it.value,
                                RequestType.AADHAAR_OTP_VERIFY
                            )
                        )
                    }

                }
            }
        }

    }

    /**
     * Confirm Auth AADHAAR OTP
     */
    fun confirmAadhaarAuthOtp(verifyOOtpRequestModel: VerifyOtpRequestModel) {
        viewModelScope.launch {
            confirmAadhaarOtpUsecase.execute(verifyOOtpRequestModel).collect {
                when (it) {
                    HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.VerifyAuthOtpRequested)
                    }
                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.CONFIRM_AUTH_AADHAAR_OTP
                            )
                        )

                    }
                    is HqResponseModel.AbdmError -> {
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                it.value,
                                RequestType.CONFIRM_AUTH_AADHAAR_OTP
                            )
                        )
                    }

                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.CONFIRM_AUTH_AADHAAR_OTP
                            )
                        )
                    }
                }

            }
        }

    }

    /**
     * Confirm Auth MOBILE OTP
     */
    fun confirmMobileAuthOtp(verifyOOtpRequestModel: VerifyOtpRequestModel) {
        viewModelScope.launch {
            confirmMobileOtpUsecase.execute(verifyOOtpRequestModel).collect {
                when (it) {
                    HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.VerifyAuthOtpRequested)
                    }
                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.CONFIRM_AUTH_MOBILE_OTP
                            )
                        )
                    }
                    is HqResponseModel.AbdmError -> {
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                it.value,
                                RequestType.CONFIRM_AUTH_MOBILE_OTP
                            )
                        )
                    }

                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.CONFIRM_AUTH_MOBILE_OTP
                            )
                        )
                    }
                }
            }
        }

    }

    /**
     * Fetch Authentication methods
     */
    fun getAuthenticationMethods(healthId: String) {
        viewModelScope.launch {
            authenticationMethodsUsecase.execute(healthId).collect {
                when (it) {
                    HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.Loading(true))
                    }

                    is HqResponseModel.Success -> {
                        if (it.value.has("authMethods")) {
                            uiState.emit(
                                GenerateAbhaUiState.Success(
                                    it.value,
                                    RequestType.AUTH_METHODS
                                )
                            )
                        } else {
                            uiState.emit(
                                GenerateAbhaUiState.Error(
                                    JsonObject(),
                                    RequestType.AUTH_METHODS
                                )
                            )
                        }

                    }
                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.AUTH_METHODS
                            )
                        )
                    }
                    is HqResponseModel.AbdmError -> {
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                it.value,
                                RequestType.AUTH_METHODS
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Fetch Translations
     */
    fun getTranslation(langCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            translationUseCase.execute(langCode)?.let {
                LanguageManager.translationModel = it
                uiState.emit(GenerateAbhaUiState.TranslationReceived)
            } ?: Timber.d("Unable to fetch translations ")
        }
    }

    //-------------------------- DATASTORE

    /**
     * Reset block state
     */
    fun clearOtpRequestState(key: String) {
        abhaRequestModel.value?.let {
            viewModelScope.launch {
                saveDataUsecase.executeFetch(PrefKeys.OTP_REQUEST.getKey()).first {
                    if (it != null) {
                        val savedJson = Gson().fromJson(it, JsonObject::class.java)
                        if (savedJson.get(key) != null) {
                            savedJson.remove(key)
                            Timber.d("----- OTP STATE  CLEARED ------ \n ${key}")
                            saveData(PrefKeys.OTP_REQUEST.getKey(), savedJson.toString())
                        }
                    }
                    true
                }
            }
        }
    }

    fun getOtpRequestCount(key:String) = flow {
            saveDataUsecase.executeFetch(PrefKeys.OTP_REQUEST.getKey()).first {
                if (it != null) {
                    val savedJson = Gson().fromJson(it, JsonObject::class.java)
                    if (savedJson.get(key) != null) {
                        val otpRequestModel = Gson().fromJson(
                            savedJson.get(key).asString,
                            OtpRequestCallModel::class.java
                        )

                        emit(otpRequestModel.counter)
                        Timber.d("----- OTP COUNT => ${otpRequestModel.counter} ------ \n ${key}")
                    }
                }else {
                    emit(0)
                }
                true
            }
    }

    /**
     * Reset block state
     */
    fun clearBlockState(key: String) {
        abhaRequestModel.value?.let {
            viewModelScope.launch {
                saveDataUsecase.executeFetch(PrefKeys.OTP_REQUEST.getKey()).first {
                    val savedJson: JsonObject
                    val otpRequestModel: OtpRequestCallModel

                    if (it != null) {
                        savedJson = Gson().fromJson(it, JsonObject::class.java)

                        if (savedJson.get(key) != null) {
                            otpRequestModel = Gson().fromJson(
                                savedJson.get(key).asString,
                                OtpRequestCallModel::class.java
                            )
                            otpRequestModel.reset()
                            savedJson.remove(key)
                            savedJson.addProperty(key, Gson().toJson(otpRequestModel))
                            Timber.d("----- OTP STATE  CLEARED ------ \n ${savedJson}")

                            saveData(PrefKeys.OTP_REQUEST.getKey(), savedJson.toString())
                        }
                    }

                    true
                }

            }
        }
    }

    /**
     * Get data from data store
     */
    fun getData(key: Preferences.Key<String>): String? {
        var value: String? = null
        viewModelScope.launch {
            val job = async {
                saveDataUsecase.executeFetch(key).collect {
                    Timber.d("OTP TS : ${it}")
                    value = it
                }
            }

            job.await()

        }
        return value
    }

    /**
     * Save data in data store
     */
    private fun saveData(key: Preferences.Key<String>, value: String) {
        saveDataUsecase.executeSave(value, key)
    }

    /**
     * Save OTP CALL Request
     */
    private fun saveOtpRequestCallCount(key: String) {
        abhaRequestModel.value?.let {
            viewModelScope.launch {
                saveDataUsecase.executeFetch(PrefKeys.OTP_REQUEST.getKey()).first {
                    val savedJson: JsonObject
                    val otpRequestModel: OtpRequestCallModel

                    if (it != null) {
                        savedJson = Gson().fromJson(it, JsonObject::class.java)

                        if (savedJson.get(key) != null) {
                            otpRequestModel = Gson().fromJson(
                                savedJson.get(key).asString,
                                OtpRequestCallModel::class.java
                            )
                            otpRequestModel.increaseOtpCounter()
                            savedJson.remove(key)
                            savedJson.addProperty(key, Gson().toJson(otpRequestModel))
                        } else {
                            otpRequestModel = OtpRequestCallModel(key, 1)
                            savedJson.addProperty(
                                otpRequestModel.id,
                                Gson().toJson(otpRequestModel)
                            )
                        }
                    } else {
                        otpRequestModel = OtpRequestCallModel(key, 1)
                        savedJson = JsonObject()
                        savedJson.addProperty(otpRequestModel.id, Gson().toJson(otpRequestModel))
                    }
                    Timber.d("----- OTP STATE ------ \n ${savedJson}")

                    saveData(PrefKeys.OTP_REQUEST.getKey(), savedJson.toString())

                    true
                }

            }
        }
    }

    /**
     * Check for BLOCKED KEYS
     */
    fun checkForBlockedState(key: String) = flow {
        saveDataUsecase.executeFetch(PrefKeys.OTP_REQUEST.getKey()).first {
            if (it != null) {
                val savedJson = Gson().fromJson(it, JsonObject::class.java)
                if (savedJson.has(key)) {
                    val otpRequestCallModel = Gson().fromJson(
                        savedJson.get(key).asString,
                        OtpRequestCallModel::class.java
                    )

                    if (otpRequestCallModel.isBlocked()) {
                        emit(OtpCallState.OtpReqBlocked(otpRequestCallModel))
                    } else {
                        emit(OtpCallState.OtpReqAvailable)
                    }

                    //Try unblocking
                    val wasUnblocked = otpRequestCallModel.tryUnBlocking()

                    if(wasUnblocked){
                        savedJson.remove(key)
                        savedJson.addProperty(key, Gson().toJson(otpRequestCallModel))
                        saveData(PrefKeys.OTP_REQUEST.getKey(), savedJson.toString())
                    }

                } else {
                    emit(OtpCallState.OtpReqAvailable)
                }
            } else {
                emit(OtpCallState.OtpReqAvailable)
            }
            true
        }
    }

    /**
     * Check if ABHA address is available
     */
    fun checkForAbhaAvailability(abhaId :String){
       viewModelScope.launch {
           abhaAvailbilityUsecase.execute(AbhaVerificationRequestModel(abhaId)).collect{
               when (it) {
                   HqResponseModel.Loading -> {
                       uiState.emit(GenerateAbhaUiState.Loading(true))
                   }
                   is HqResponseModel.Success -> {
                       uiState.emit(
                           GenerateAbhaUiState.Success(
                               it.value,
                               RequestType.ABHA_AVAILABILITY
                           )
                       )
                   }
                   is HqResponseModel.Error -> {
                       uiState.emit(
                           GenerateAbhaUiState.Error(
                               it.value,
                               RequestType.ABHA_AVAILABILITY
                           )
                       )
                   }
                   else -> {
                       //exhaustive block
                   }
               }
           }
       }
    }

    /**
     * Fetch  ABHA Card
     */
    fun fetchAbhaCard(userToken :String){
        viewModelScope.launch {
            fetchAbhaCardUseCase.execute(AbhaCardRequestModel(userToken)).collect{
                when (it) {
                    HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.Loading(true))
                    }
                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.FETCH_ABHA_CARD
                            )
                        )
                    }
                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.FETCH_ABHA_CARD
                            )
                        )
                    }
                    else -> {
                        //exhaustive block
                    }
                }
            }
        }
    }

}

/**
 * OTP Call State
 */

sealed class OtpCallState {
    class OtpReqBlocked(val otpRequestCallModel: OtpRequestCallModel) : OtpCallState()
    object OtpReqAvailable : OtpCallState()
}

///**
// * UI State
// */
//sealed class GenerateAbhaUiState {
//    data class Loading(val isLoading: Boolean) : GenerateAbhaUiState()
//    object TranslationReceived : GenerateAbhaUiState()
//    object ValidState : GenerateAbhaUiState()
//    object InvalidState : GenerateAbhaUiState()
//    object MobileOtpRequested : GenerateAbhaUiState()
//    object AadhaarOtpRequested : GenerateAbhaUiState()
//    object AuthOtpRequested : GenerateAbhaUiState()
//    object VerifyAuthOtpRequested : GenerateAbhaUiState()
//    object VerifyMobileOtpRequested : GenerateAbhaUiState()
//    object VerifyAadhaarOtpRequested : GenerateAbhaUiState()
//    object AbhaAvailabilityRequested : GenerateAbhaUiState()
//    object Blocked : GenerateAbhaUiState()
//
//    data class Success(val data: JsonObject, val requestType: RequestType) :
//        GenerateAbhaUiState()
//
//    data class Error(val data: JsonObject, val requestType: RequestType) : GenerateAbhaUiState()
//    data class AbdmError(val data: AbdmErrorModel, val requestType: RequestType) :
//        GenerateAbhaUiState()
//}

///**
// * Request type sent
// */
//enum class RequestType {
//    MOBILE_OTP,
//    MOBILE_OTP_VERIFY,
//
//    AADHAAR_OTP,
//    AADHAAR_OTP_VERIFY,
//
//    AUTH_METHODS,
//
//    GENERATE_AUTH_OTP,
//    VERIFY_AUTH_OTP,
//
//    CONFIRM_AUTH_AADHAAR_OTP,
//    CONFIRM_AUTH_MOBILE_OTP,
//
//    ABHA_AVAILABILITY,
//    FETCH_ABHA_CARD
//}
