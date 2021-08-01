package com.flow.android.kotlin.lockscreen.fluidcontentresize

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.Window

data class ActivityViewHolder(
    val decorView: ViewGroup,
    val contentView: ViewGroup,
    val frameLayout: ViewGroup
) {
    companion object {
        fun createFrom(activity: Activity): ActivityViewHolder {
            val decorView = activity.window.decorView as ViewGroup
            val contentView = decorView.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
            val actionBarOverlayLayout = contentView.parent as ViewGroup
            val frameLayout = actionBarOverlayLayout.parent as ViewGroup

            return ActivityViewHolder(
                decorView = decorView,
                contentView = contentView,
                frameLayout = frameLayout)
        }
    }

    fun onDetach(onDetach: () -> Unit) {
        decorView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(view: View?) {
                onDetach()
            }

            override fun onViewAttachedToWindow(view: View?) {  }
        })
    }
}