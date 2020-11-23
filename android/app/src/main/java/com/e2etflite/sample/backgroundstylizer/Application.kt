package com.e2etflite.sample.backgroundstylizer

import android.app.Application
import com.e2etflite.sample.backgroundstylizer.di.segmentationAndStyleTransferModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            //androidContext(applicationContext)
            androidContext(this@Application)
            modules(
                segmentationAndStyleTransferModule
            )
        }

    }

}