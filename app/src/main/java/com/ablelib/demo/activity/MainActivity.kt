package com.ablelib.demo.activity

import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ablelib.demo.R
import com.ablelib.demo.adapter.PagesAdapter
import com.ablelib.listeners.PermissionRequestResult
import com.ablelib.manager.IAbleManager
import com.ablelib.util.AbleLogOptions
import com.ablelib.util.QualityOfService
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    val ableManager: IAbleManager by inject()

    private val tabIcons = arrayOf(
        R.drawable.ic_antenna,
        R.drawable.ic_list,
        R.drawable.ic_comm,
        R.drawable.ic_service,
        android.R.drawable.ic_lock_idle_charging,
        R.drawable.ic_settings
    )

    private val selectedTabColor: Int
        get() = ContextCompat.getColor(this, R.color.colorBlue)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ableManager.handlePermissionRequestIfNotGranted(this)
        ableManager.importBondedDevices()

        ableManager.loggingOptions = AbleLogOptions.Full
        ableManager.handlePermissionRequestIfNotGranted(this)
        ableManager.qualityOfService = QualityOfService.LOW_ENERGY

        viewPager.adapter = PagesAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = tabIcons.size
        tabLayout.setupWithViewPager(viewPager)
        for (i in 0..tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.icon = ContextCompat.getDrawable(this, tabIcons[i])
        }
        tabLayout.getTabAt(0)?.icon?.setColorFilter(selectedTabColor, PorterDuff.Mode.SRC_IN)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.icon?.colorFilter = null
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(selectedTabColor, PorterDuff.Mode.SRC_IN)
            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ableManager.handleRequestPermissionResult(requestCode, permissions, grantResults, object: PermissionRequestResult {
            override fun onAllPermissionsGranted() {
                //No need to handle if accepted
            }

            override fun onPermissionDenied(permissionDenied: Array<String>) {
                Toast.makeText(this@MainActivity, "Permission is required to use the app", Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }
}