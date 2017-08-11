package com.github.nekdenis.weatherlogger

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {

    lateinit var controller: MainController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inject()
        controller.start(this)
    }

    private fun inject() {
        controller = com.github.nekdenis.weatherlogger.utils.mainController()
    }


    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }
}
