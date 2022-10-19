package org.commcare.dalvik.domain.repositories

import org.commcare.dalvik.domain.model.TranslationModel

interface TranslationRepository {
    suspend fun getTranslationData(langCode:String) :TranslationModel?
}