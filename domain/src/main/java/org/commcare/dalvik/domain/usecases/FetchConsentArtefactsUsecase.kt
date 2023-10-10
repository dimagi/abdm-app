package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.ConsentArtefactFilterModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class FetchConsentArtefactsUsecase @Inject constructor(
    val repository: AbdmRepository
) {

    lateinit var filterModel: ConsentArtefactFilterModel

    fun initFilter(consentReqId: String) {
        this.filterModel = ConsentArtefactFilterModel(consentReqId)
    }

    fun updateFilter(
        filterText: String? = null
    ) {
        filterModel.let {
            it.filterText = filterText
        }

    }


    suspend fun execute(page:Int?) =
        repository.getConsentArtefacts(filterModel.consentRequestId,filterModel.filterText ,page)

    fun getConsentPagerData() = repository.getConsentArtefactPagerData(this)

}