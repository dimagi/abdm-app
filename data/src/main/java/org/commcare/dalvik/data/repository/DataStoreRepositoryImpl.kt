package org.commcare.dalvik.data.repository

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commcare.dalvik.data.util.DataStoreUtil
import org.commcare.dalvik.domain.repositories.DatastoreRepository
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(val dsUtil: DataStoreUtil) : DatastoreRepository {

    override fun saveData(key: Preferences.Key<String>, value: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dsUtil.saveToDataStore(key, value)
        }
    }

    override  fun getData(key: Preferences.Key<String>) = dsUtil.getFromDataStore(key)

    override fun removeKey(key: Preferences.Key<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            dsUtil.removeFromStore(key)
        }
    }


}