package com.ablelib.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.ablelib.demo.R
import com.ablelib.models.AbleDevice
import kotlinx.android.synthetic.main.list_item_device_comm.view.*

class CommAdapter(private val context: Context, val devices: List<AbleDevice>): BaseAdapter() {

    var onConnectToDevice: OnConnectToDevice? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_device_comm, null, true)
        val device = devices[position]
        view.deviceName.text = "${device.name} - ${device.address}"
        view.connectButton.setOnClickListener {
            onConnectToDevice?.onConnectToDevice(devices[position])
        }
        return view
    }


    override fun getItem(position: Int) = devices[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = devices.size

    interface OnConnectToDevice {
        fun onConnectToDevice(device: AbleDevice)
    }
}