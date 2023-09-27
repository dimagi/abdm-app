package org.commcare.dalvik.abha.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.commcare.dalvik.domain.usecases.ConfirmCareContextOtpUsecase
import org.commcare.dalvik.domain.usecases.FetchCareContextAuthModeUsecase
import org.commcare.dalvik.domain.usecases.GenerateCareContextOtpUsecase
import org.commcare.dalvik.domain.usecases.LinkCareContextUsecase
import javax.inject.Inject

@HiltViewModel
class CareContextViewModel @Inject constructor(
    confirmCareContextOtpUsecase: ConfirmCareContextOtpUsecase,
    fetchCareContextAuthModeUsecase: FetchCareContextAuthModeUsecase,
    generateCareContextOtpUsecase: GenerateCareContextOtpUsecase,
    linkCareContextUsecase: LinkCareContextUsecase
) : BaseViewModel() {

    fun fetchCareContextAuthModes(){
        viewModelScope.launch {

        }

    }

    fun generateCareContextOtp(){
        viewModelScope.launch {

        }
    }

    fun confirmCareContextOtp(){
        viewModelScope.launch {

        }
    }

    fun linkCareContext(){
        viewModelScope.launch {

        }
    }


}