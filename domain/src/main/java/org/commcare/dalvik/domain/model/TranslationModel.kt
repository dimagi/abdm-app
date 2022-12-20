package org.commcare.dalvik.domain.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import timber.log.Timber


data class TranslationModel(
    val meta: Meta,

    @SerializedName("app_data")
    val data: JsonObject?,

    @SerializedName("abdm_health_data")
    val healthData: JsonObject?,

    ) {

    fun getTranslatedString(key: String): String {
        Timber.d("Translating DATA => ${data}")
        Timber.d("Translating HEALTH DATA  => ${healthData}")
        Timber.d("Translating Key  => ${key}")
        try {
            return data?.let {
                data[key]?.let {
                    return it.asString
                } ?: healthData?.let {
                    healthData[key]?.let {
                        return it.asString
                    }
                } ?: LanguageManager.getDefaultTranslation(key)

            } ?: LanguageManager.getDefaultTranslation(key)


        } catch (e: Exception) {
            return LanguageManager.getDefaultTranslation(key)
        }
    }

}

class Meta(val code: String, val language: String)





