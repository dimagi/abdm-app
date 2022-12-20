package org.commcare.dalvik.data.services


import org.commcare.dalvik.domain.model.TranslationModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface TranslationService {
    @GET
    suspend fun getTranslationData(@Url url: String ): Response<TranslationModel>
}