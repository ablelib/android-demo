package com.ablelib.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.ablelib.demo.R
import com.ablelib.models.AbleDevice

class ScannedDevicesAdapter(private val context: Context, val devices: List<AbleDevice>): BaseAdapter() {

    var onConnectToDevice: OnConnectToDevice? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_device_scan, null, true) as TextView
        val device = devices[position]
        view.text = "${device.name} - ${device.address}"
        return view
    }


    override fun getItem(position: Int) = devices[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = devices.size

    interface OnConnectToDevice {
        fun onConnectToDevice(device: AbleDevice)
    }
}