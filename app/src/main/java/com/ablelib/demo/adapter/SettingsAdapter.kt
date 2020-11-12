package com.ablelib.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.ablelib.AbleManager
import com.ablelib.demo.R
import com.ablelib.util.QualityOfService
import kotlinx.android.synthetic.main.list_item_settings.view.*


class SettingsAdapter(private val context: Context, val qualityOfServices: List<QualityOfService>): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_settings, null, true)
        val quality = qualityOfServices[position]
        view.qualityOfServiceText.text = "QualityOfService(delayBetwenScans:${quality.delayBetweenScans}, scanTimeout:${quality.scanTimeout}, scanOptions: nil)"

        val selectedQuality = AbleManager.qualityOfService
        view.checkmark.visibility = if (selectedQuality == quality) View.VISIBLE else View.GONE
        return view
    }

    override fun getItem(position: Int) = qualityOfServices[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = qualityOfServices.size
}