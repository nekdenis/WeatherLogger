package com.github.nekdenis.weatherlogger.main

import android.app.Activity
import android.os.Bundle
import com.github.nekdenis.weatherlogger.R

class MainActivity : Activity() {

    lateinit var controller: MainController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inject()
        controller.onStart()
    }

    private fun inject() {
        controller = com.github.nekdenis.weatherlogger.utils.mainController()
    }


    override fun onDestroy() {
        super.onDestroy()
        controller.onStop()
    }
}
