package org.commcare.dalvik.domain.usecases

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import org.commcare.dalvik.domain.repositories.DatastoreRepository
import javax.inject.Inject

class SaveDataUsecase @Inject constructor(val repository: DatastoreRepository) {

    fun executeSave(value: String, prefKey: Preferences.Key<String>) =
        repository.saveData(prefKey, value)

    fun executeFetch( prefKey: Preferences.Key<String>): Flow<String?> {
        return repository.getData(prefKey)
    }

    fun removeKey(prefKey: Preferences.Key<String>){
        repository.removeKey(prefKey)
    }
}