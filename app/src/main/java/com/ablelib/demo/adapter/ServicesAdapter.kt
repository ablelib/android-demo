package com.ablelib.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.ablelib.demo.R
import kotlinx.android.synthetic.main.list_item_device_service.view.*

class ServicesAdapter(private val context: Context, var services: List<String>): BaseAdapter() {
    enum class Type {
        SERVICE,
        CHARACTERISTIC
    }

    var type = Type.SERVICE
    var serviceSelectedListener: OnServiceSelected? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_device_service, null, true)
        val service = services[position]
        view.uuidText.text = service
        view.typeText.text = when (type) {
            Type.SERVICE -> "Discover services"
            Type.CHARACTERISTIC -> "Discover characteristics"
        }

        view.setOnClickListener {
            if (type == Type.SERVICE) {
                serviceSelectedListener?.onServiceSelected(services[position])
            }
        }

        return view
    }

    override fun getItem(position: Int) = services[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = services.size

    interface OnServiceSelected {
        fun onServiceSelected(uuid: String)
    }
}