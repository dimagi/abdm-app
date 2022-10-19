package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.AadhaarOtpRequestModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject


class RequestAadhaarOtpUsecase @Inject constructor(val repository: AbdmRepository) {

     fun execute(aadhaarNumber: String)  =
        repository.generateAadhaarOtp(AadhaarOtpRequestModel(aadhaarNumber))

}