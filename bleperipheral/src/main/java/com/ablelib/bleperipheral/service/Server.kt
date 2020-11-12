package com.ablelib.bleperipheral.service

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.ablelib.bleperipheral.MyApplication
import java.text.SimpleDateFormat
import java.util.*

class Server private constructor() {
    private var server: BluetoothGattServer? = null
    private var bluetoothManager: BluetoothManager? = null
    val devices = mutableListOf<BluetoothDevice>()

    fun start() {
        bluetoothManager = MyApplication.appContext
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        startAdvertising()
        startServer()
    }

    private val serverCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                device?.let {
                    devices.add(it)
                }
            }
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)

            server?.sendResponse(
                device,
                requestId,
                offset,
                0,
                SUCCESS.toByteArray()
            )
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {

            Log.d("Server", "onDescriptorWriteRequest")
            server?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                SUCCESS.toByteArray()
            )

        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?
        ) {
            Log.d("Server", "onCharacteristicWriteRequest")

            if (characteristic?.uuid == TimeService.TIME_CHARACTERISTIC) {
                Log.d("Server", "notify")
                //Send current date as string
                val receivedFormat = String(characteristic?.value!!)
                val timeFormat = SimpleDateFormat(receivedFormat)
                characteristic?.value = timeFormat.format(Date()).toByteArray()
                for (connectedDevice in devices) {
                    server?.notifyCharacteristicChanged(connectedDevice, characteristic, false)
                }
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            server?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                Date().toString().toByteArray()
            )
        }
    }

    private fun startAdvertising() {
        val bluetoothAdvertiser = bluetoothManager?.adapter?.bluetoothLeAdvertiser
        bluetoothAdvertiser?.let {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(ParcelUuid(TimeService.TIME_SERVICE))
                .build()

            it.startAdvertising(settings, data, object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    Log.d("server", "start success")
                }

                override fun onStartFailure(errorCode: Int) {
                    Log.d("server", "start failure $errorCode")
                }
            })
        }
    }

    private fun startServer() {
        server =
            bluetoothManager?.openGattServer(MyApplication.appContext, serverCallback)
        server?.addService(TimeService.getTimeService())
    }

    companion object {
        val instance: Server by lazy { Server() }
        private val SUCCESS = "success"
    }
}