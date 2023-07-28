package org.commcare.dalvik.abha.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.WindowManager
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
        setupActivityListener()
    }


    private fun setupActivityListener() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    companion object{
        var API_TOKEN = ""
    }
}