package com.rvodevelopment.tuinmaat.util

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

object ActivityProvider {
    private var activityRef: WeakReference<FragmentActivity>? = null

    fun setCurrentActivity(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
    }

    fun getCurrentActivity(): FragmentActivity? = activityRef?.get()
}
