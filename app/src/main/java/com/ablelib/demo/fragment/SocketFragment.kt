package com.ablelib.demo.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.ablelib.comm.AbleCommParams
import com.ablelib.comm.AbleCoroutineComm
import com.ablelib.demo.R
import com.ablelib.manager.IAbleManager
import com.ablelib.models.AbleDevice
import com.ablelib.models.AbleUUID
import com.ablelib.sockets.IAbleSocket
import com.ablelib.sockets.IAbleSocketConnection
import com.ablelib.sockets.l2cap.AblePsm
import com.ablelib.sockets.l2cap.L2capSocket
import kotlinx.android.synthetic.main.fragment_scan.listView
import kotlinx.android.synthetic.main.fragment_scan.scanningLayout
import kotlinx.android.synthetic.main.fragment_scan.startScan
import kotlinx.android.synthetic.main.fragment_scan.stopScan
import kotlinx.android.synthetic.main.fragment_socket.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class SocketFragment : DeviceListFragment() {
    val ableManager: IAbleManager by inject()

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    private var comm: AbleCoroutineComm? = null
    private var socket: IAbleSocket? = null
    private var socketConn: IAbleSocketConnection? = null

    private val serviceId = AbleUUID("12E61727-B41A-436F-B64D-4777B35F2294")
    private val charId = AbleUUID("ABDD3056-28FA-441D-A470-55A75A52553A")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_socket, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setList(listView)
        startScan.setOnClickListener {
            startScan()
        }

        stopScan.setOnClickListener {
            ableManager.stopScan()
        }

        disconnect.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                closeSocket()
                withContext(Dispatchers.Main) {
                    scan.visibility = View.VISIBLE
                    communication.visibility = View.GONE
                    startScan.visibility = View.VISIBLE
                }
            }
        }

        send.setOnClickListener {
            socketConn?.send(textToSend.text.toString().toByteArray())
        }
    }

    override fun onDeviceClick(device: AbleDevice) {
        CoroutineScope(Dispatchers.IO).launch {
            doComm(device)
        }
    }

    private fun startScan() {
        scope.launch {
            startScan.visibility = View.GONE
            scanningLayout.visibility = View.VISIBLE
            updateList(ableManager.scan(5000))
            scanningLayout.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun doComm(device: AbleDevice) {
        try {
            comm = AbleCoroutineComm(ableManager, AbleCommParams(device))
            comm?.run {
                log("Connecting...")
                connect()
                withContext(Dispatchers.Main) {
                    scan.visibility = View.GONE
                    communication.visibility = View.VISIBLE
                }
                log("Connected")
                val services = discoverServices()
                val psmService = services.first {
                    it.uuid == serviceId
                }
                log("Found PSM service")
                val psmChar = psmService.characteristics.first {
                    it.uuid == charId
                }
                log("Found PSM characteristic")
                val data = readCharacteristic(psmChar).value
                val psm = AblePsm(data)
                log("Read PSM: $psm")
                socket = L2capSocket(false, device, psm)
                log("Opening client socket")
                socketConn = socket?.connect()?.apply {
                    log("Client socket connected")
                    onSend = { result ->
                        result.onSuccess {
                            logSync("Sent data: $it")
                        }.onFailure {
                            logSync("Error sending data: ${it.message}")
                        }
                    }
                    onReceive = { result ->
                        result.onSuccess {
                            activity?.runOnUiThread {
                                val text = String(it, Charsets.UTF_8)
                                receivedFromServer.text =
                                    receivedFromServer.text.toString() + "\n$text"
                                logSync("Received data: $it - $text")
                            }
                        }.onFailure {
                            logSync("Error receiving data: ${it.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log("Error while communicating: ${e.message}")
        }
    }

    private suspend fun closeSocket() {
        try {
            socketConn?.close()
            log("Socket closed")
            comm = null
            socket = null
        } catch (e: Exception) {
            log("Error closing socket: ${e.message}")
        }
    }

    private fun logSync(message: String) {
        activity?.runOnUiThread {
            clientLog.text = clientLog.text.toString() + "\n$message"
        }
    }
    
    private suspend fun log(message: String) {
        withContext(Dispatchers.Main) {
            logSync(message)
        }
    }
}