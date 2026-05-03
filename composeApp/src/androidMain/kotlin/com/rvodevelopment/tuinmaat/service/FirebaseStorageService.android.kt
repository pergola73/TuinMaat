package com.rvodevelopment.tuinmaat.service

import dev.gitlive.firebase.storage.StorageReference
import dev.gitlive.firebase.storage.Data

actual suspend fun StorageReference.uploadBytes(bytes: ByteArray) {
    this.putData(Data(bytes))
}
