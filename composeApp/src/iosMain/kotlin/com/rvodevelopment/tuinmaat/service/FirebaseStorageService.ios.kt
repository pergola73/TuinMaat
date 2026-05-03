package com.rvodevelopment.tuinmaat.service

import dev.gitlive.firebase.storage.StorageReference
import dev.gitlive.firebase.storage.Data
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun StorageReference.uploadBytes(bytes: ByteArray) {
    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    // In gitlive-firebase iOS, Data constructor takes NSData
    this.putData(Data(nsData))
}
