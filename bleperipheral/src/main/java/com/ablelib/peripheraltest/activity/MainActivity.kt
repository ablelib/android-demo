package com.ablelib.peripheraltest.activity

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.os.ParcelUuid
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ablelib.comm.AbleGattConnectionState
import com.ablelib.listeners.PermissionRequestResult
import com.ablelib.manager.AbleManager
import com.ablelib.models.AbleCharacteristic
import com.ablelib.models.AbleService
import com.ablelib.models.AbleUUID
import com.ablelib.peripheral.AbleGattServer
import com.ablelib.peripheral.AbleGattServerBuilder
import com.ablelib.peripheraltest.R
import com.ablelib.sockets.IAbleSocketConnection
import com.ablelib.sockets.l2cap.AblePsm
import com.ablelib.sockets.l2cap.L2capServerSocket
import com.ablelib.util.AbleLogOptions
import com.ablelib.util.QualityOfService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    private val ableManager = AbleManager.shared
    private var server: AbleGattServer? = null
    private var socket: L2capServerSocket? = null
    private var socketConn: IAbleSocketConnection? = null
    private lateinit var serverBuilder: AbleGattServerBuilder
    private var char: AbleCharacteristic? = null
    private var myPsm: AblePsm? = null

    private val serviceId = AbleUUID("12E61727-B41A-436F-B64D-4777B35F2294")
    private val charId = AbleUUID("ABDD3056-28FA-441D-A470-55A75A52553A")
    private val descId = AbleUUID("00002902-0000-1000-8000-00805f9b34fb")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ableManager.loggingOptions = AbleLogOptions.Full
        ableManager.handlePermissionRequestIfNotGranted(this)
        ableManager.qualityOfService = QualityOfService.LOW_ENERGY

        serverBuilder = {
            service(serviceId, AbleService.Type.PRIMARY) {
                char = characteristic(charId,
                    setOf(AbleCharacteristic.Property.READ, AbleCharacteristic.Property.INDICATE),
                    setOf(AbleCharacteristic.Permission.READ)) {
                    descriptor(descId, setOf(AbleCharacteristic.Permission.READ,
                        AbleCharacteristic.Permission.WRITE
                    ))
                }
            }
            autoManageDevices()
            onReadCharacteristic(charId) { request ->
                myPsm?.let { psm ->
                    val data = psm.getData()
                    request.characteristic.value = data
                    respondTo(request.withValue(data), AbleGattServer.Status.SUCCESS)
                }
            }

            onConnectionStateChanged { device, state, i ->
                if (state == AbleGattConnectionState.CONNECTED) {
                    char?.let { char ->
                        myPsm?.let { psm ->
                            notifyCharacteristicChanged(
                                device,
                                char,
                                psm.getData(),
                                false
                            )
                        }
                    }
                }
            }

            onWriteDescriptor(descId) { request, _, _ ->
                respondTo(request, AbleGattServer.Status.SUCCESS)
            }
        }

        startServer.setOnClickListener {
            server = if (server == null) {
                startServer()
            } else {
                stopServer()
                null
            }
            startServer.setText(if (server == null) R.string.start_server else R.string.stop_server)
            comm.visibility = if (server == null) View.INVISIBLE else View.VISIBLE
        }

        send.setOnClickListener {
            val text = textToSend.text.toString()
            if (text.isNotBlank()) {
                socketConn?.send(text.toByteArray())
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ableManager.handleRequestPermissionResult(requestCode,
            permissions, grantResults, object : PermissionRequestResult {
                override fun onAllPermissionsGranted() {
                }

                override fun onPermissionDenied(permissionDenied: Array<String>) {
                }
            })
    }

    private fun startServer(): AbleGattServer {
        ableManager.startAdvertising(
            AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build(),
            AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(ParcelUuid(serviceId.toUUID))
                .build()) {
            log("Start advertising?: $it")
        }
        return ableManager.startGattServer(serverBuilder).apply {
            log("Started server")
            CoroutineScope(Dispatchers.IO).launch {
                socket = L2capServerSocket(false).apply {
                    myPsm = open()
                    withContext(Dispatchers.Main) { log("Server socket open with PSM: $myPsm") }
                    socketConn = accept().apply {
                        withContext(Dispatchers.Main) { log("Client socket accepted") }
                        onSend = { result ->
                            runOnUiThread {
                                result.onSuccess {
                                    log("Sent data: $it - ${String(it, Charsets.UTF_8)}")
                                }.onFailure {
                                    log("Error sending data: ${it.message}")
                                }
                            }
                        }
                        onReceive = { result ->
                            runOnUiThread {
                                result.onSuccess {
                                    val text = String(it, Charsets.UTF_8)
                                    log("Received data: $it - $text")
                                    receivedFromClient.text =
                                        receivedFromClient.text.toString() + "\n$text"
                                }.onFailure {
                                    log("Error receiving data: ${it.message}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopServer() {
        ableManager.stopAdvertising()
        log("Stopped advertising.")
        server?.close()
        log("Stopped server")
        CoroutineScope(Dispatchers.IO).launch {
            socket?.close()
            withContext(Dispatchers.Main) { log("Closed socket") }
            socket = null
            socketConn = null
        }
    }

    private fun log(message: String) {
        serverLog.text = serverLog.text.toString() + "\n$message"
    }
}

fun AblePsm.getData(): ByteArray {
    return ByteBuffer.allocate(Int.SIZE_BYTES).apply {
        order(ByteOrder.LITTLE_ENDIAN)
    }.putInt(value).array()
}