package com.ablelib.demo.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ablelib.demo.fragment.CommFragment
import com.ablelib.demo.fragment.ScanFragment
import com.ablelib.demo.fragment.SettingsFragment
import com.ablelib.demo.fragment.StorageFragment

class PagesAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {

    private val fragments = arrayOf(ScanFragment(), StorageFragment(), CommFragment(), SettingsFragment())

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount() = fragments.size
}