package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.AbhaVerificationRequestModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class AbhaAvailabilityUsecase @Inject constructor(val repository: AbdmRepository) {
    fun execute(abhaVerificationModel: AbhaVerificationRequestModel) =
        repository.checkAbhaAvailability(abhaVerificationModel)
}