package com.ablelib.bleperipheral.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ablelib.bleperipheral.R
import com.ablelib.bleperipheral.service.Server
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startServer.setOnClickListener {
            Server.instance.start()
            startServer.text = getString(R.string.server_is_running)
            startServer.setOnClickListener(null)
        }
    }
}