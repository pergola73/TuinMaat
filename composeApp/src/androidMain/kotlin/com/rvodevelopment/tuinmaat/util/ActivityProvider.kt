package com.rvodevelopment.tuinmaat.util

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

object ActivityProvider {
    private var activityRef: WeakReference<FragmentActivity>? = null
    private var onActivityResultCallback: ((Int, Int, Intent?) -> Unit)? = null

    fun setCurrentActivity(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
    }

    fun getCurrentActivity(): FragmentActivity? = activityRef?.get()

    fun setActivityResultCallback(callback: ((Int, Int, Intent?) -> Unit)?) {
        onActivityResultCallback = callback
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onActivityResultCallback?.invoke(requestCode, resultCode, data)
    }
}
