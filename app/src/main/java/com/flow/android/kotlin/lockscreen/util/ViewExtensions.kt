package com.flow.android.kotlin.lockscreen.util

import android.animation.AnimatorListenerAdapter
import android.view.View

fun View.rotate(
        degrees: Float, duration: Number,
        animationListenerAdapter: AnimatorListenerAdapter? = null
) {
    this.animate().rotation(degrees)
            .setDuration(duration.toLong())
            .setListener(animationListenerAdapter)
            .start()
}