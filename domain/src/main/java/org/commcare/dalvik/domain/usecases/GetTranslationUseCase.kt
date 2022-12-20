package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.repositories.TranslationRepository
import javax.inject.Inject

class GetTranslationUseCase @Inject constructor(val repository: TranslationRepository)  {
    suspend fun execute(languageCode: String)  =
        repository.getTranslationData(languageCode)
}