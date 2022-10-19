package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.model.MobileOtpRequestModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class RequestMobileOtpUseCase @Inject constructor(val repository: AbdmRepository)   {

     fun execute(mobileNumber: String , txnId:String) =
        repository.generateMobileOtp(MobileOtpRequestModel(mobileNumber,txnId))

}