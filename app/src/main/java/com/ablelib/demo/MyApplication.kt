package com.ablelib.demo

import android.app.Application
import com.ablelib.AbleManager

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AbleManager.shared.initialize(this)
    }
}