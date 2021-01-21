package com.ablelib.demo

import android.app.Application
import com.ablelib.demo.di.ableModule
import com.ablelib.manager.AbleManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(ableModule)
        }
        AbleManager.shared.initialize(this)
    }
}