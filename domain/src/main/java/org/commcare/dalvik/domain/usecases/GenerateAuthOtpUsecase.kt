package org.commcare.dalvik.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.commcare.dalvik.domain.model.GenerateAuthOtpModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

class GenerateAuthOtpUsecase @Inject constructor(private val repository: AbdmRepository) {
    fun execute(healthId: String, authMethod: String) :Flow<HqResponseModel> {
        val generateAuthOtp = GenerateAuthOtpModel(healthId ,authMethod)
        return repository.generateAuthOtp(generateAuthOtp)
    }
}