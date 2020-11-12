package com.ablelib.demo.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ablelib.AbleManager
import com.ablelib.demo.R
import com.ablelib.listeners.AbleNearbyScanListener
import com.ablelib.models.AbleDevice
import com.ablelib.pair
import com.ablelib.demo.adapter.ScannedDevicesAdapter
import kotlinx.android.synthetic.main.fragment_scan.*

class ScanFragment : Fragment() {

    private var devices = mutableListOf<AbleDevice>()
    private lateinit var adapter: ScannedDevicesAdapter
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        startScan.setOnClickListener {
            scanningLayout.visibility = View.VISIBLE
            it.visibility = View.GONE
            startScan()
        }

        stopScan.setOnClickListener {
            AbleManager.stopScan()
        }

        adapter = ScannedDevicesAdapter(context!!, devices)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedDevice = devices[position]
            progressDialog = ProgressDialog.show(context!!, "Pairing", "Pairing in progress")
            clickedDevice.pair {
                progressDialog.dismiss()
            }
        }
    }

    private fun startScan() {
        AbleManager.scan(15_000, object: AbleNearbyScanListener {
            override fun onStart() {
                //Not Implmented
                devices.clear()
            }

            override fun onDeviceFound(device: AbleDevice) {
                devices.add(device)
                adapter.notifyDataSetChanged()
                Log.e("OnDeviceFound", "device found")
            }

            override fun onStop() {
                activity?.runOnUiThread {scanningLayout.visibility = View.GONE}
            }

            override fun onError(e: Exception) {
                Log.e("ScanFragment", e.localizedMessage)
            }

        })
    }
}