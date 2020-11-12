package com.ablelib.demo.fragment

import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ablelib.comm.AbleCoroutineComm
import com.ablelib.comm.BluetoothGattConnectionState
import com.ablelib.comm.comm
import com.ablelib.demo.R
import com.ablelib.models.AbleDevice
import com.ablelib.storage.AbleDeviceStorage
import com.ablelib.demo.adapter.CommAdapter
import com.ablelib.demo.adapter.ServicesAdapter
import kotlinx.android.synthetic.main.fragment_comm.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import java.util.*

class CommFragment: Fragment() {

    //Test comm UUIDs
    val TIME_SERVICE = UUID.fromString("7c2a9688-43ba-4254-83ad-cc9df6deb72b")
    val TIME_CHARACTERISTIC = UUID.fromString("e4eada67-6da6-4bdd-841a-1489a633cd1f")
    val CONFIG_DESCRIPTOR = UUID.fromString("4a134627-a123-410e-b5d2-08c30219c52f")

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private var devices = mutableListOf<AbleDevice>()
    private lateinit var adapter: CommAdapter
    private var deviceComm: AbleCoroutineComm? = null
    private var services = listOf<BluetoothGattService>()

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

        testComm.setOnClickListener {
            testCommLayout.visibility = View.VISIBLE
        }

        writeDescriptorButton.setOnClickListener {
            launch {
                services = deviceComm?.discoverServices()!!
                val timeService = services.find { service -> service.uuid == TIME_SERVICE }
                    ?: return@launch

                val timeChar = timeService.getCharacteristic(TIME_CHARACTERISTIC) ?: return@launch
                val configDescriptor = timeChar.getDescriptor(CONFIG_DESCRIPTOR) ?: return@launch
                deviceComm?.writeDescriptor(timeChar, configDescriptor, false)
            }
        }

        requestTimeButton.setOnClickListener {
            launch {
                val timeService = services.find { service -> service.uuid == TIME_SERVICE } ?: return@launch
                val timeChar = timeService.getCharacteristic(TIME_CHARACTERISTIC) ?: return@launch
                val time = deviceComm?.writeCharacteristic(timeChar, "h:mm a".toByteArray())
                timeText.text = String(time!!.value)
            }
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
            if (state == BluetoothGattConnectionState.CONNECTED) {
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

    private fun updateServicesList(services: List<BluetoothGattService>) {
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
        devices.addAll(AbleDeviceStorage.devices)
        adapter.notifyDataSetChanged()
    }
}