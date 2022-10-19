package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.VerifyOtpRequestModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class ConfirmMobileOtpUsecase@Inject constructor(val repository: AbdmRepository) {

    fun execute(verifyOtpRequestModel: VerifyOtpRequestModel)  =
        repository.confirmMobileOtp(verifyOtpRequestModel)

}