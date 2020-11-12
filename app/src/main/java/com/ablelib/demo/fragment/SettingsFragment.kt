package com.ablelib.demo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.ablelib.AbleManager
import com.ablelib.demo.R
import com.ablelib.util.QualityOfService
import com.ablelib.demo.adapter.SettingsAdapter
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment: Fragment() {

    private val qualities = listOf(QualityOfService.LOW_ENERGY, QualityOfService.DEFAULT, QualityOfService.INTENSIVE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = SettingsAdapter(context!!, qualities)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            AbleManager.qualityOfService = qualities[position]
            adapter.notifyDataSetChanged()
        }
    }
}