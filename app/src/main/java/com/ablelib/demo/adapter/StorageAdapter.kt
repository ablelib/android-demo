package com.ablelib.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.ablelib.demo.R
import com.ablelib.models.AbleDevice
import kotlinx.android.synthetic.main.list_item_device_storage.view.*

class StorageAdapter(private val context: Context, val devices: List<AbleDevice>): BaseAdapter() {
    var deviceRemovedListener: OnDeviceRemoved? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_device_storage, null, true)
        val device = devices[position]
        view.deviceName.text = "${device.name} - ${device.address}"
        view.deleteButton.setOnClickListener {
            deviceRemovedListener?.onDeviceRemoved(devices[position])
        }
        return view
    }


    override fun getItem(position: Int) = devices[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = devices.size

    interface OnDeviceRemoved {
        fun onDeviceRemoved(device: AbleDevice)
    }
}