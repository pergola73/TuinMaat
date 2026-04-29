package com.rvodevelopment.tuinmaat.service

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.rvodevelopment.tuinmaat.util.ActivityProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.coroutines.resume

class AndroidMediaService : MediaService {
    override suspend fun pickImage(): ByteArray? = suspendCancellableCoroutine { continuation ->
        val activity = ActivityProvider.getCurrentActivity()
        if (activity == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        ActivityProvider.setActivityResultCallback { requestCode, resultCode, data ->
            if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
                val uri = data?.data
                if (uri != null) {
                    val inputStream = activity.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    continuation.resume(bytes)
                } else {
                    continuation.resume(null)
                }
            } else if (requestCode == 1001) {
                continuation.resume(null)
            }
            ActivityProvider.setActivityResultCallback(null)
        }
        activity.startActivityForResult(intent, 1001)
    }

    override suspend fun takePhoto(): ByteArray? = suspendCancellableCoroutine { continuation ->
        val activity = ActivityProvider.getCurrentActivity()
        if (activity == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val photoFile = File(activity.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        val photoUri: Uri = FileProvider.getUriForFile(
            activity,
            "com.rvodevelopment.tuinmaat.provider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        
        ActivityProvider.setActivityResultCallback { requestCode, resultCode, data ->
            if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
                try {
                    val bytes = photoFile.readBytes()
                    // Optioneel: verklein de foto als deze te groot is voor de AI
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    continuation.resume(stream.toByteArray())
                } catch (e: Exception) {
                    continuation.resume(null)
                } finally {
                    photoFile.delete()
                }
            } else if (requestCode == 1002) {
                photoFile.delete()
                continuation.resume(null)
            }
            ActivityProvider.setActivityResultCallback(null)
        }

        try {
            activity.startActivityForResult(intent, 1002)
        } catch (e: Exception) {
            ActivityProvider.setActivityResultCallback(null)
            photoFile.delete()
            continuation.resume(null)
        }
    }
}
