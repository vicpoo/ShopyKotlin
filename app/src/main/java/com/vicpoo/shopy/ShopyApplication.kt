// ShopyApplication.kt
package com.vicpoo.shopy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShopyApplication : Application() {
    companion object {
        lateinit var instance: ShopyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}