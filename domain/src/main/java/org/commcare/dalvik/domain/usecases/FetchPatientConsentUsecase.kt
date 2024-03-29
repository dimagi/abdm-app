package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.PatientConsentFilterModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class FetchPatientConsentUsecase @Inject constructor(
    val repository: AbdmRepository
) {
    lateinit var filterModel: PatientConsentFilterModel

    fun initFilter(abhaId: String) {
        this.filterModel = PatientConsentFilterModel(abhaId)
    }

    fun updateFilter(
        filterText: String? = null,
        fromDate: String? = null,
        toDate: String? = null
    ) {
       filterModel.let {
           it.filterText = filterText
           it.fromDate = fromDate
           it.toDate = toDate
       }

    }

    suspend fun execute(position:Int?) =
        repository.getPatientConsents(filterModel.abhaId,position, filterModel.filterText, filterModel.fromDate, filterModel.toDate)

    fun getPatientConsentPagerData() = repository.getPatientConsentPagerData(this)

}