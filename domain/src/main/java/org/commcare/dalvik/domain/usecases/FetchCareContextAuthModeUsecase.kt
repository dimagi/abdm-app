package org.commcare.dalvik.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.commcare.dalvik.domain.model.CCAuthModesRequestModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class FetchCareContextAuthModeUsecase @Inject constructor(private val repository: AbdmRepository) {
    fun execute(ccAuthModesRequestModel: CCAuthModesRequestModel): Flow<HqResponseModel> {
        return repository.getCCAuthModes(ccAuthModesRequestModel)
    }
}