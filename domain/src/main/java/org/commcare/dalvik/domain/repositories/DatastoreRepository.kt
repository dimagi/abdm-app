package org.commcare.dalvik.domain.repositories

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface DatastoreRepository {
    fun saveData(key: Preferences.Key<String>, value: String)
    fun getData(key: Preferences.Key<String>): Flow<String?>
    fun removeKey(key: Preferences.Key<String>)
}