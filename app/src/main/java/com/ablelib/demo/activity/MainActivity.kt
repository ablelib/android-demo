package com.ablelib.demo.activity

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ablelib.demo.R
import com.ablelib.demo.adapter.PagesAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val tabIcons = arrayOf(R.drawable.ic_antenna, R.drawable.ic_list, R.drawable.ic_comm,
            R.drawable.ic_settings
    )

    private val selectedTabColor: Int
        get() = ContextCompat.getColor(this, R.color.colorBlue)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager.adapter = PagesAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = 4
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
}