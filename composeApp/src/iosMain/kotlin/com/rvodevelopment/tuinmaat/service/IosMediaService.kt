package com.rvodevelopment.tuinmaat.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.*
import platform.Foundation.*
import platform.PhotosUI.*
import platform.darwin.NSObject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import platform.posix.memcpy

class IosMediaService : MediaService {
    private var currentDelegate: ImagePickerDelegate? = null

    override suspend fun pickImage(): ByteArray? = suspendCancellableCoroutine { continuation ->
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        
        val delegate = ImagePickerDelegate { image ->
            currentDelegate = null
            continuation.resume(image)
        }
        
        currentDelegate = delegate
        picker.delegate = delegate
        
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }

    override suspend fun takePhoto(): ByteArray? = suspendCancellableCoroutine { continuation ->
        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        
        val delegate = ImagePickerDelegate { image ->
            currentDelegate = null
            continuation.resume(image)
        }
        
        currentDelegate = delegate
        picker.delegate = delegate
        
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }

    override suspend fun resizeImage(imageBytes: ByteArray, maxDimension: Int): ByteArray {
        return imageBytes
    }
}

private class ImagePickerDelegate(
    private val onImagePicked: (ByteArray?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    
    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val bytes = image?.let { uiImageToByteArray(it) }
        picker.dismissViewControllerAnimated(true) {
            onImagePicked(bytes)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            onImagePicked(null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun uiImageToByteArray(image: UIImage): ByteArray? {
    val data = UIImageJPEGRepresentation(image, 0.8) ?: return null
    val bytes = ByteArray(data.length.toInt())
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return bytes
}
