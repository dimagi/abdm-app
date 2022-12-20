package org.commcare.dalvik.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.commcare.dalvik.data.network.NetworkUtil
import org.commcare.dalvik.data.services.TranslationService
import org.commcare.dalvik.domain.model.LanguageManager
import org.commcare.dalvik.domain.model.TranslationModel
import org.commcare.dalvik.domain.repositories.TranslationRepository
import timber.log.Timber
import javax.inject.Inject

class TranslationRepositoryImpl @Inject constructor(private val translationService: TranslationService) :
    TranslationRepository {
    override suspend fun getTranslationData(langCode: String): TranslationModel? {
        val job = CoroutineScope(Dispatchers.IO).async {
            Timber.d("LANG URL => ${NetworkUtil.getTranslationEndpoint(langCode)}")
            val response =
                translationService.getTranslationData(NetworkUtil.getTranslationEndpoint(langCode))
            response.body()
        }

        return try {
            Timber.d("LANG => @@@@@ Waiting for result ${job.await()}")
            job.await()
        } catch (e: Exception) {
            Timber.d("LANG => XXXX Fallback to DEFAULT JSON  =  ${e.message}")
            Gson().fromJson(
                LanguageManager.DEFAULT_TRANSLATIONS,
                TranslationModel::class.java
            )
        }

    }
}