package com.ablelib.demo.di

import com.ablelib.manager.AbleManager
import com.ablelib.manager.IAbleManager
import com.ablelib.storage.AbleDeviceStorage
import com.ablelib.storage.IAbleDeviceStorage
import org.koin.dsl.module

val ableModule = module {
    single<IAbleManager> { AbleManager.shared }
    single<IAbleDeviceStorage> { AbleDeviceStorage.default }
}
