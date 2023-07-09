package org.commcare.dalvik.abha.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.commcare.dalvik.domain.model.AbdmErrorModel
import org.commcare.dalvik.domain.model.AbhaScanModel
import org.commcare.dalvik.domain.model.AbhaVerificationRequestModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.usecases.AbhaAvailabilityUsecase
import javax.inject.Inject

@HiltViewModel
class ScanAbhaViewModel @Inject constructor(private val abhaAvailbilityUsecase: AbhaAvailabilityUsecase) :
    BaseViewModel() {
    var abhaScanModel: MutableLiveData<AbhaScanModel> = MutableLiveData()
    val uiState = MutableStateFlow<GenerateAbhaUiState>(GenerateAbhaUiState.InvalidState)

    /**
     * Check if ABHA address is available
     */
    fun checkForAbhaAvailability() {
       abhaScanModel.value?.getAbha()?.let {abhaId ->
           viewModelScope.launch {
               abhaAvailbilityUsecase.execute(AbhaVerificationRequestModel(abhaId)).collect {
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
                   }
               }
           }
       }
    }

    sealed class UiState {
        data class Loading(val isLoading: Boolean) : GenerateAbhaUiState()
        data class Success(val data: JsonObject, val requestType: RequestType) :
            GenerateAbhaUiState()

        data class Error(val data: JsonObject, val requestType: RequestType) : GenerateAbhaUiState()
        data class AbdmError(val data: AbdmErrorModel, val requestType: RequestType) :
            GenerateAbhaUiState()
    }

}