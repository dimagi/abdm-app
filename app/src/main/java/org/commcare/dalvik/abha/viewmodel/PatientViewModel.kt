package org.commcare.dalvik.abha.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.model.AbhaRequestModel
import org.commcare.dalvik.abha.model.FilterModel
import org.commcare.dalvik.abha.ui.main.fragment.ACCESS_MODE
import org.commcare.dalvik.abha.ui.main.fragment.PURPOSE
import org.commcare.dalvik.abha.utility.PropMutableLiveData
import org.commcare.dalvik.domain.model.ConsentPermission
import org.commcare.dalvik.domain.model.Hiu
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.model.Patient
import org.commcare.dalvik.domain.model.PatientConsentDetailModel
import org.commcare.dalvik.domain.model.Purpose
import org.commcare.dalvik.domain.model.Requester
import org.commcare.dalvik.domain.usecases.FetchPatientConsentUsecase
import org.commcare.dalvik.domain.usecases.SubmitPatientConsentUsecase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(private val submitPatientConsentUseCase: SubmitPatientConsentUsecase,
                                           private val fetchPatientConsentUsecase: FetchPatientConsentUsecase) :
    BaseViewModel() {
    lateinit var patientConsentModel: PatientConsentDetailModel
    val uiState = MutableStateFlow<GenerateAbhaUiState>(GenerateAbhaUiState.InvalidState)
    var filterModel: PropMutableLiveData<FilterModel> = PropMutableLiveData()

    fun init(patientId: String, hiuId: String) {
        this.patientConsentModel =
            PatientConsentDetailModel(Purpose(PURPOSE.CAREMGT.name)).apply {
                hiu = Hiu(hiuId)
                patient = Patient(patientId)
                requester = Requester("Dr. Manju")
                permission = ConsentPermission(ACCESS_MODE.VIEW.value)
            }

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

    fun fetchPatientConsent() = fetchPatientConsentUsecase.getPatientConsent().cachedIn(viewModelScope)
}
