package com.github.nekdenis.weatherlogger.core.firebase

import com.github.nekdenis.weatherlogger.core.system.DeviceInfo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

interface FirebaseRootReferenceHolder {
    val rootReference: DatabaseReference
}

class FirebaseRootReferenceHolderImpl(db: FirebaseDatabase, root: String, deviceInfo: DeviceInfo) : FirebaseRootReferenceHolder {
    private val serial: String = deviceInfo.deviceId()
    override val rootReference: DatabaseReference by lazy { db.getReference("${deviceInfo.buildFlavor()}/$serial/$root").apply { keepSynced(false) } }
}