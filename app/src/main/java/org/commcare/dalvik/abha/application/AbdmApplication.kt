package org.commcare.dalvik.abha.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.commcare.dalvik.abha.BuildConfig
import org.commcare.dalvik.domain.model.LanguageManager
import timber.log.Timber


@HiltAndroidApp
class AbdmApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        LanguageManager.init()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object{
        var API_TOKEN = ""
    }
}