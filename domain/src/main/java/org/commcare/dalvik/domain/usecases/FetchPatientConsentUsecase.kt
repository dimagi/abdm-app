package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class FetchPatientConsentUsecase @Inject constructor(
    val repository: AbdmRepository
) {
    suspend fun execute(
        abhaId: String,
        searchText: String? = null,
        fromDate: String? = null,
        toDate: String? = null
    ) =
        repository.getPatientConsents(abhaId, searchText, fromDate, toDate)

    fun getPatientConsent() = repository.getPatientConsent(this)


}