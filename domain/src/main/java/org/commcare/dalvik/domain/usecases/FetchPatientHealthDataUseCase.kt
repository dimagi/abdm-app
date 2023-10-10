package org.commcare.dalvik.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class FetchPatientHealthDataUseCase @Inject constructor(private val repository: AbdmRepository) {
    fun execute(artefactId: String,transactionId:String?,page:Int?): Flow<HqResponseModel> {
        return repository.getPatientHealthData(artefactId,transactionId,page)
    }
}