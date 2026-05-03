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
    // We gebruiken de platform-specifieke NSData zoals de server verwacht
    // De cast naar Any en dan naar Data is om de lokale compiler tevreden te houden
    @Suppress("UNCHECKED_CAST")
    this.putData(nsData as Any as Data)
}
