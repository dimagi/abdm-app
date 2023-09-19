package org.commcare.dalvik.abha.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.model.FilterModel
import org.commcare.dalvik.abha.ui.main.fragment.ACCESS_MODE
import org.commcare.dalvik.abha.ui.main.fragment.PURPOSE
import org.commcare.dalvik.abha.utility.PropMutableLiveData
import org.commcare.dalvik.domain.model.ConsentPermission
import org.commcare.dalvik.domain.model.HealthContentModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.model.IdNameModel
import org.commcare.dalvik.domain.model.Patient
import org.commcare.dalvik.domain.model.PatientConsentDetailModel
import org.commcare.dalvik.domain.model.Purpose
import org.commcare.dalvik.domain.model.Requester
import org.commcare.dalvik.domain.usecases.FetchConsentArtefactsUsecase
import org.commcare.dalvik.domain.usecases.FetchPatientConsentUsecase
import org.commcare.dalvik.domain.usecases.FetchPatientHealthDataUseCase
import org.commcare.dalvik.domain.usecases.SubmitPatientConsentUsecase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val submitPatientConsentUseCase: SubmitPatientConsentUsecase,
    private val fetchPatientConsentUsecase: FetchPatientConsentUsecase,
    private val fetchConsentArtefactsUsecase: FetchConsentArtefactsUsecase,
    private val fetchPatientHealthDataUseCase: FetchPatientHealthDataUseCase
) :
    BaseViewModel() {
    lateinit var patientAbhaId:String
    lateinit var patientConsentModel: PatientConsentDetailModel
    val uiState = MutableStateFlow<GenerateAbhaUiState>(GenerateAbhaUiState.InvalidState)
    var consentFilterModel: PropMutableLiveData<FilterModel> = PropMutableLiveData()
    var consentArtefactFilterModel: PropMutableLiveData<FilterModel> = PropMutableLiveData()
    lateinit var patientHealthData:Pair<String,MutableList<HealthContentModel>>


    fun init(patientId: String, hiuId: String) {
        this.patientConsentModel =
            PatientConsentDetailModel(Purpose(PURPOSE.CAREMGT.name)).apply {
                hiu = IdNameModel(hiuId)
                patient = Patient(patientId)
                requester = Requester("Dr. Manju")
                permission = ConsentPermission(ACCESS_MODE.VIEW.value)
            }
    }

    fun initPatientFilterModel(abhaId: String) {
        fetchPatientConsentUsecase.initFilter(abhaId)
        consentFilterModel.setValue(FilterModel())
    }

    fun initArtefactFilterModel(consentReqId: String) {
        fetchConsentArtefactsUsecase.initFilter(consentReqId)
        consentFilterModel.setValue(FilterModel())
    }

    fun submitPatientConsent() {
        val consentJson = Gson().toJson(patientConsentModel)
        Timber.d(
            "Consent JSON ===>  ${consentJson} "
        )

        viewModelScope.launch {
            submitPatientConsentUseCase.execute(patientConsentModel).collect {
                when (it) {
                    HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.InvalidState)
                        uiState.emit(GenerateAbhaUiState.Loading(true))
                    }

                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.CREATE_PATIENT_CONSENT
                            )
                        )
                    }

                    is HqResponseModel.Error -> {
                        uiState.emit(
                            GenerateAbhaUiState.Error(
                                it.value,
                                RequestType.CREATE_PATIENT_CONSENT
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

    fun fetchPatientConsent() =
        fetchPatientConsentUsecase.getPatientConsentPagerData().cachedIn(viewModelScope)

    fun fetchConsentArtefacts() =
        fetchConsentArtefactsUsecase.getConsentPagerData().cachedIn(viewModelScope)

    fun fetchPatientHealthData(artefactsId:String,transactionId:String?,page:Int?) {

        viewModelScope.launch {
            fetchPatientHealthDataUseCase.execute(artefactsId,transactionId,page).collect{
                when(it){
                    is HqResponseModel.Loading -> {
                        uiState.emit(GenerateAbhaUiState.AadhaarOtpRequested)
                    }
                    is HqResponseModel.Success -> {
                        uiState.emit(
                            GenerateAbhaUiState.Success(
                                it.value,
                                RequestType.FETCH_CONSENT_ARTEFACTS
                            )
                        )
                    }
                    is HqResponseModel.Error -> {
                        Timber.d("EMIT Sending -> GenerateAbhaUiState.Error")
//                        uiState.emit(
//                            GenerateAbhaUiState.Error(
//                                it.value,
//                                RequestType.AADHAAR_OTP
//                            )
//                        )
                    }
                    is HqResponseModel.AbdmError -> {
                        Timber.d("EMIT Sending -> GenerateAbhaUiState.AbdmError")
//                        uiState.emit(
//                            GenerateAbhaUiState.AbdmError(
//                                it.value,
//                                RequestType.AADHAAR_OTP
//                            )
//                        )
                    }
                }
            }
        }
    }


    fun updatePatientFilter() {
        consentFilterModel.value?.let {
            fetchPatientConsentUsecase.updateFilter(
                filterText = it.filterText,
                toDate = it.toDate,
                fromDate = it.fromDate
            )
        }
    }

    fun initPatientAbhaId(abhaId: String) {
        patientAbhaId = abhaId
        initPatientFilterModel(patientAbhaId)
    }

    fun resetPatientConsent(abhaId: String) {
        initPatientAbhaId(abhaId)
    }

}
