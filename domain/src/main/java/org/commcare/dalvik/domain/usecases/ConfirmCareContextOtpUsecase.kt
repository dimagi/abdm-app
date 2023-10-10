package org.commcare.dalvik.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.commcare.dalvik.domain.model.ConfirmAuthModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class ConfirmCareContextOtpUsecase @Inject constructor(private val repository: AbdmRepository) {

    fun execute(confirmAuthModel: ConfirmAuthModel): Flow<HqResponseModel> {
        return repository.confirmCCAuthenticationOtp(confirmAuthModel)
    }

}