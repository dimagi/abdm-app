package org.commcare.dalvik.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.commcare.dalvik.domain.model.CCLinkModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class LinkCareContextUsecase @Inject constructor(private val repository: AbdmRepository) {

    fun execute(ccLinkModel: CCLinkModel): Flow<HqResponseModel> {
        return repository.linkCareContext(ccLinkModel)
    }

}