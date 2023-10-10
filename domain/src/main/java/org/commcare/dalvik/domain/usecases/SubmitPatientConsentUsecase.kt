package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.PatientConsentDetailModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class SubmitPatientConsentUsecase @Inject constructor(val repository: AbdmRepository) {
    fun execute(patientConsentDetailModel: PatientConsentDetailModel) =
        repository.submitPatientConsent(patientConsentDetailModel)
}