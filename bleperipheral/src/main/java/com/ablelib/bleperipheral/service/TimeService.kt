package com.ablelib.bleperipheral.service

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.*

object TimeService {
    val TIME_SERVICE = UUID.fromString("7c2a9688-43ba-4254-83ad-cc9df6deb72b")
    val TIME_CHARACTERISTIC = UUID.fromString("e4eada67-6da6-4bdd-841a-1489a633cd1f")
    val CLIENT_CONFIG = UUID.fromString("4a134627-a123-410e-b5d2-08c30219c52f")

    fun getTimeService(): BluetoothGattService {
        val service = BluetoothGattService(TIME_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val partyCharacteristic = BluetoothGattCharacteristic(
            TIME_CHARACTERISTIC,
            PROPERTY_READ or PROPERTY_WRITE or PROPERTY_NOTIFY, PERMISSION_READ or PERMISSION_WRITE)
        val configDescriptor = BluetoothGattDescriptor(CLIENT_CONFIG, PERMISSION_READ or PERMISSION_WRITE)
        partyCharacteristic.addDescriptor(configDescriptor)
        service.addCharacteristic(partyCharacteristic)
        return service
    }
}