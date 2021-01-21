package com.ablelib.demo.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ablelib.demo.R
import com.ablelib.manager.AbleManager
import com.ablelib.manager.pair
import com.ablelib.models.AbleDevice
import kotlinx.android.synthetic.main.fragment_scan.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ScanFragment : DeviceListFragment() {
    private lateinit var progressDialog: ProgressDialog
    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setList(listView)
        startScan.setOnClickListener {
            startScan()
        }

        stopScan.setOnClickListener {
            AbleManager.shared.stopScan()
        }
    }

    override fun onDeviceClick(device: AbleDevice) {
        progressDialog = ProgressDialog.show(context!!, "Pairing", "Pairing in progress")
        device.pair {
            progressDialog.dismiss()
        }
    }

    private fun startScan() {
        scope.launch {
            startScan.visibility = View.GONE
            scanningLayout.visibility = View.VISIBLE
            updateList(AbleManager.shared.scan(15_000))
            scanningLayout.visibility = View.GONE
        }
    }
}