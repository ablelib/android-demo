package com.ablelib.demo.fragment

import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.ablelib.demo.adapter.ScannedDevicesAdapter
import com.ablelib.models.AbleDevice

abstract class DeviceListFragment: Fragment() {
    protected lateinit var adapter: ScannedDevicesAdapter
    private val deviceList = mutableListOf<AbleDevice>()
    private var deviceListView: ListView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = ScannedDevicesAdapter(context!!, deviceList)
    }

    fun setList(list: ListView) {
        deviceListView = list
        deviceListView?.adapter = adapter
        deviceListView?.setOnItemClickListener { _, _, position, _ ->
            onDeviceClick(deviceList[position])
        }
    }

    open protected fun onDeviceClick(device: AbleDevice) {
        //To override by fragment if needed
    }

    protected fun updateList(collection: Collection<AbleDevice>) {
        deviceList.clear()
        deviceList.addAll(collection)
        adapter.notifyDataSetChanged()
    }
}