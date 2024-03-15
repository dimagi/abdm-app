package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.PatientNotificationModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class NotifyPatientUsecase @Inject constructor(val repository: AbdmRepository)   {

    fun execute(patientNotificationModel: PatientNotificationModel) =
        repository.notifyPatient(patientNotificationModel)

}