package com.github.nekdenis.weatherlogger.core.system

import android.content.ContentResolver
import android.os.Build
import android.provider.Settings
import com.github.nekdenis.weatherlogger.BuildConfig

interface DeviceInfo {
    fun deviceName(): String
    fun deviceId(): String
    fun buildFlavor(): String
}

class DeviceInfoImpl(val contentResolver: ContentResolver) : DeviceInfo {
    override fun deviceId(): String = if (Build.SERIAL == "unknown")
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    else Build.SERIAL

    override fun deviceName(): String = "${android.os.Build.BRAND} ${android.os.Build.MODEL}"

    override fun buildFlavor(): String = BuildConfig.BUILD_TYPE
}
