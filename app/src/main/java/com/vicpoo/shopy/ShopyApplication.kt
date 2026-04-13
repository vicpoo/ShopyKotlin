// ShopyApplication.kt
package com.vicpoo.shopy

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.vicpoo.shopy.domain.work.WorkManagerHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShopyApplication : Application() {

    @Inject
    lateinit var workManagerHelper: WorkManagerHelper

    companion object {
        lateinit var instance: ShopyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializar WorkManager
        workManagerHelper.scheduleAllWork()

        Log.d("ShopyApplication", "✅ App iniciada con WorkManager")
    }
}