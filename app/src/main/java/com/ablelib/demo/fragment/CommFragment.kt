package com.ablelib.demo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ablelib.comm.AbleCoroutineComm
import com.ablelib.comm.AbleGattConnectionState
import com.ablelib.comm.comm
import com.ablelib.demo.R
import com.ablelib.demo.adapter.CommAdapter
import com.ablelib.demo.adapter.ServicesAdapter
import com.ablelib.models.AbleDevice
import com.ablelib.models.AbleService
import com.ablelib.storage.AbleDeviceStorage
import kotlinx.android.synthetic.main.fragment_comm.*
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

class CommFragment: Fragment() {
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private var devices = mutableListOf<AbleDevice>()
    private lateinit var adapter: CommAdapter
    private var deviceComm: AbleCoroutineComm? = null
    private var services = listOf<AbleService>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comm, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initList()

        refresh.setOnClickListener {
            refresh()
        }
        refresh()

        disconnect.setOnClickListener {
            disconnect()
        }

        discoverServices.setOnClickListener {
            discoverServices()
        }
    }

    private fun initList() {
        adapter = CommAdapter(context!!, devices)
        listView.adapter = adapter
        adapter.onConnectToDevice = object : CommAdapter.OnConnectToDevice {
            override fun onConnectToDevice(device: AbleDevice) {
                connectToDevice(device)
            }

        }
    }

    private fun connectToDevice(device: AbleDevice) {
        deviceComm = device.comm
        launch {
            val state = deviceComm!!.connect()
            if (state == AbleGattConnectionState.CONNECTED) {
                updateLayouts(true)
            }
        }
    }

    private fun disconnect() {
        deviceComm = null
        discoverServices.visibility = View.VISIBLE
        updateLayouts(false)
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        uiScope.launch(EmptyCoroutineContext, CoroutineStart.DEFAULT, block)
    }

    private fun updateLayouts(connected: Boolean) {
        connectedLayout.visibility = if (connected) View.VISIBLE else View.GONE
        notConnectedLayout.visibility = if (connected) View.GONE else View.VISIBLE
    }

    private fun discoverServices() {
        deviceComm?.let {
            launch {
                services = it.discoverServices()
                updateServicesList(services)
            }
        }

        discoverServices.visibility = View.GONE
    }

    private fun updateServicesList(services: List<AbleService>) {
        val list = services.map { service ->
            service.uuid.toString()
        }
        val adapter = ServicesAdapter(context!!, list)
        adapter.serviceSelectedListener = object : ServicesAdapter.OnServiceSelected {
            override fun onServiceSelected(uuid: String) {
                val service = services.find { service -> service.uuid.toString() == uuid }
                val characteristics = service?.characteristics?.map { characteristic ->
                    characteristic.uuid.toString()
                }
                adapter.services = characteristics!!
                adapter.type = ServicesAdapter.Type.CHARACTERISTIC
                adapter.notifyDataSetChanged()
            }

        }
        servicesList.adapter = adapter
    }

    private fun refresh() {
        devices.clear()
        devices.addAll(AbleDeviceStorage.default.devices)
        adapter.notifyDataSetChanged()
    }
}