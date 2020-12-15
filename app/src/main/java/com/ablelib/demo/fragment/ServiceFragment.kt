package com.ablelib.demo.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ablelib.AbleManager
import com.ablelib.demo.R
import com.ablelib.demo.activity.MainActivity
import com.ablelib.models.AbleDevice
import com.ablelib.services.AbleService
import kotlinx.android.synthetic.main.fragment_service.*

class ServiceFragment : DeviceListFragment() {

    private val devices = mutableSetOf<AbleDevice>()
    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("Receiver", "onReceive")
            intent?.getParcelableExtra<AbleDevice>(AbleService.DEVICE)?.let {
                devices.add(it)
                updateList(devices)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_service, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setList(listView)
        startService.setOnClickListener {
            startService()
        }
    }

    override fun onResume() {
        activity?.registerReceiver(receiver, IntentFilter(AbleService.ACTION_DEVICE_FOUND))
        super.onResume()
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun startService() {
        AbleManager.shared.setUpService(
            serviceClass = AbleService::class.java,
            notification = AbleService.defaultNotification(
                context!!,
                getString(R.string.app_name),
                getString(R.string.app_is_scanning),
                R.drawable.ic_launcher_foreground,
                MainActivity::class.java
            ),
            backgroundScanInApp = true,
            backgroundScanOutsideApp = true
        )

        AbleManager.shared.refreshBackgroundServiceState()
        scanningLayout.visibility = View.VISIBLE
        startService.visibility = View.GONE
    }
}