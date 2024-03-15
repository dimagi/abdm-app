package org.commcare.dalvik.abha.viewmodel

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.model.LinkCareContextModel
import org.commcare.dalvik.data.util.PrefKeys
import org.commcare.dalvik.domain.model.ConfirmAuthModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.model.OtpRequestCallModel
import org.commcare.dalvik.domain.usecases.ConfirmCareContextOtpUsecase
import org.commcare.dalvik.domain.usecases.FetchCareContextAuthModeUsecase
import org.commcare.dalvik.domain.usecases.GenerateCareContextOtpUsecase
import org.commcare.dalvik.domain.usecases.LinkCareContextUsecase
import org.commcare.dalvik.domain.usecases.SaveDataUsecase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CareContextViewModel @Inject constructor(
    val confirmCareContextOtpUsecase: ConfirmCareContextOtpUsecase,
    val fetchCareContextAuthModeUsecase: FetchCareContextAuthModeUsecase,
    val generateCareContextOtpUsecase: GenerateCareContextOtpUsecase,
    val linkCareContextUsecase: LinkCareContextUsecase,
    val saveDataUsecase: SaveDataUsecase,
) : BaseViewModel() {

    val authModesList = mutableListOf<String>()
    lateinit var linkCareContextModel: LinkCareContextModel
    val uiState = MutableStateFlow<GenerateAbhaUiState>(GenerateAbhaUiState.InvalidState)
    val otpRequestBlocked: MutableLiveData<OtpRequestCallModel> = MutableLiveData()
    lateinit var selectedAuthMethod: String
    lateinit var confirmAuthModel: ConfirmAuthModel


    fun init(model: LinkCareContextModel) {
        linkCareContextModel = model
    }

    fun resetUiState() {
        viewModelScope.launch {
            uiState.emit(GenerateAbhaUiState.Loading(false))
        }
    }

    fun fetchCareContextAuthModes() {
        viewModelScope.launch {
            linkCareContextModel.getAuthModesRequestModel()?.let {
                val authModeFlow = fetchCareContextAuthModeUsecase.execute(it)
                authModeFlow.collect {
                    when (it) {
                        HqResponseModel.Loading -> {
                            uiState.emit(GenerateAbhaUiState.InvalidState)
                            uiState.emit(GenerateAbhaUiState.Loading(true))
                        }

                        is HqResponseModel.Success -> {
                            uiState.emit(
                                GenerateAbhaUiState.Success(
                                    it.value,
                                    RequestType.CC_AUTH_METHODS
                                )
                            )
                        }

                        is HqResponseModel.Error -> {
                            uiState.emit(
                                GenerateAbhaUiState.Error(
                                    it.value,
                                    RequestType.CC_AUTH_METHODS
                                )
                            )
                        }

                        is HqResponseModel.AbdmError -> {
                            uiState.emit(
                                GenerateAbhaUiState.AbdmError(
                                    it.value,
                                    RequestType.CC_AUTH_METHODS
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun generateCareContextOtp() {
        viewModelScope.launch {
            selectedAuthMethod.let { authMethod ->
                linkCareContextModel.getAuthModesRequestModel(authMethod)?.let { ccAuthReqModel ->
                    //save otp req call count only if not via demographic option here
                    saveOtpRequestCallCount(linkCareContextModel.hipId)

                    generateCareContextOtpUsecase.execute(ccAuthReqModel).collect { hqRespModel ->
                        when (hqRespModel) {
                            HqResponseModel.Loading -> {
                                uiState.emit(GenerateAbhaUiState.CCOtpRequested)
                            }

                            is HqResponseModel.Success -> {
                                uiState.emit(
                                    GenerateAbhaUiState.Success(
                                        hqRespModel.value,
                                        RequestType.CC_AUTH_INIT
                                    )
                                )
                            }

                            is HqResponseModel.Error -> {
                                uiState.emit(
                                    GenerateAbhaUiState.Error(
                                        hqRespModel.value,
                                        RequestType.CC_AUTH_INIT
                                    )
                                )
                            }

                            is HqResponseModel.AbdmError -> {
                                uiState.emit(
                                    GenerateAbhaUiState.AbdmError(
                                        hqRespModel.value,
                                        RequestType.CC_AUTH_INIT
                                    )
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    fun confirmCareContextOtp() {
        viewModelScope.launch {
            confirmCareContextOtpUsecase.execute(confirmAuthModel).collect{hqRespModel ->
                when (hqRespModel) {
                    HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.CCVerifyOtpRequested)
                    }

                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                hqRespModel.value,
                                RequestType.CC_AUTH_CONFIRM
                            )
                        )
                    }

                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                hqRespModel.value,
                                RequestType.CC_AUTH_CONFIRM
                            )
                        )
                    }

                    is HqResponseModel.AbdmError -> {
                        uiState.emit(
                            GenerateAbhaUiState.AbdmError(
                                hqRespModel.value,
                                RequestType.CC_AUTH_CONFIRM
                            )
                        )
                    }
                }
            }
        }
    }

    fun linkCareContext(accessToken:String) {
        viewModelScope.launch {

            linkCareContextModel.getCCLinkRequestModel(accessToken)?.let { linkModel ->
                linkCareContextUsecase.execute(linkModel).collect{hqRespModel ->
                    when (hqRespModel) {
                        HqResponseModel.Loading -> {
                            uiState.emit(GenerateAbhaUiState.CCLinkRequested)
                        }

                        is HqResponseModel.Success -> {
                            uiState.emit(
                                GenerateAbhaUiState.Success(
                                    hqRespModel.value,
                                    RequestType.CC_LINK
                                )
                            )
                        }

                        is HqResponseModel.Error -> {
                            uiState.emit(
                                GenerateAbhaUiState.Error(
                                    hqRespModel.value,
                                    RequestType.CC_LINK
                                )
                            )
                        }

                        is HqResponseModel.AbdmError -> {
                            uiState.emit(
                                GenerateAbhaUiState.AbdmError(
                                    hqRespModel.value,
                                    RequestType.CC_LINK
                                )
                            )
                        }
                    }
                }

            }

        }
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
                Timber.d("----- CC ---> OTP COUNTER SAVED ------ \n ${savedJson}")

                saveData(PrefKeys.OTP_REQUEST.getKey(), savedJson.toString())

                true
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

}