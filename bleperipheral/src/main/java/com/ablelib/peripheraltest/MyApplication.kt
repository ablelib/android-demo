package com.ablelib.peripheraltest

import android.app.Application
import android.content.Context
import com.ablelib.manager.AbleManager

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        AbleManager.shared.initialize(this)
    }

    companion object {
        lateinit var appContext: Context
    }
}