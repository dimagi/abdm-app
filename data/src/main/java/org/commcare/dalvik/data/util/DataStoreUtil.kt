package org.commcare.dalvik.data.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DataStoreUtil @Inject constructor(@ApplicationContext val context: Context) {


    private val USER_PREFERENCES_NAME = "abdm_pref"

    private val Context.dataStore by preferencesDataStore(
        name = USER_PREFERENCES_NAME
    )


    suspend fun saveToDataStore(prefKey: Preferences.Key<String>, prefValue: String) {
        context.dataStore.edit {
            it[prefKey] = prefValue
        }
    }

    fun getFromDataStore(prefKey: Preferences.Key<String>) =
        context.dataStore.data.map {
            it[prefKey]
        }


    suspend fun clearDataStore() {
        context.dataStore.edit {
            it.clear()
        }
    }

    suspend fun removeFromStore(prefKey: Preferences.Key<String>) {
        context.dataStore.edit { preference ->
            preference.remove(prefKey)
        }
    }

}

enum class PrefKeys {
    AADHAAR_OTP_REQ_TS {
        override fun getKey(): Preferences.Key<String> {
            return stringPreferencesKey("AADHAAR_OTP_REQ_TS")
        }

    },
    MOBILE_OTP_REQ_TS {
        override fun getKey(): Preferences.Key<String> {
            return stringPreferencesKey("MOBILE_OTP_REQ_TS")
        }

    },
    OTP_BLOCKED_TS {
        override fun getKey(): Preferences.Key<String> {
            return stringPreferencesKey("OTP_BLOCKED_TS")
        }

    },
    OTP_REQUEST {
        override fun getKey(): Preferences.Key<String> {
            return stringPreferencesKey("OTP_REQUEST")
        }

    };

    abstract fun getKey(): Preferences.Key<String>
}