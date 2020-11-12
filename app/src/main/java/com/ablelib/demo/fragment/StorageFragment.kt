package com.ablelib.demo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ablelib.demo.R
import com.ablelib.models.AbleDevice
import com.ablelib.storage.AbleDeviceStorage
import com.ablelib.demo.adapter.StorageAdapter
import kotlinx.android.synthetic.main.fragment_storage.*

class StorageFragment : Fragment() {

    private var devices = mutableListOf<AbleDevice>()
    private lateinit var adapter: StorageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_storage, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initList()
        refresh.setOnClickListener {
            refresh()
        }
        refresh()
    }

    private fun initList() {
        adapter = StorageAdapter(context!!, devices)
        adapter.deviceRemovedListener = object: StorageAdapter.OnDeviceRemoved {
            override fun onDeviceRemoved(device: AbleDevice) {
                devices.remove(device)
                AbleDeviceStorage.remove(device)
                adapter.notifyDataSetChanged()
            }

        }
        listView.adapter = adapter
    }

    private fun refresh() {
        devices.clear()
        devices.addAll(AbleDeviceStorage.devices)
        adapter.notifyDataSetChanged()
    }
}