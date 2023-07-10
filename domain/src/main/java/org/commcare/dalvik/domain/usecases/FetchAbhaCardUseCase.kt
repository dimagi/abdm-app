package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.AbhaCardRequestModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class FetchAbhaCardUseCase  @Inject constructor(val repository: AbdmRepository) {
    fun execute(abhaCardRequestModel: AbhaCardRequestModel) =
        repository.fetchAbhaCard(abhaCardRequestModel)
}