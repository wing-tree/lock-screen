package com.flow.android.kotlin.lockscreen.fluidcontentresize

import android.view.ViewTreeObserver

object KeyboardVisibilityDetector {

    fun listen(viewHolder: ActivityViewHolder, listener: (KeyboardVisibilityChanged) -> Unit) {
        val detector = Detector(viewHolder, listener)
        viewHolder.decorView.viewTreeObserver.addOnPreDrawListener(detector)
        viewHolder.onDetach {
            viewHolder.decorView.viewTreeObserver.removeOnPreDrawListener(detector)
        }
    }

    private class Detector(
        val viewHolder: ActivityViewHolder,
        val onKeyboardVisibilityChanged: (KeyboardVisibilityChanged) -> Unit
    ) : ViewTreeObserver.OnPreDrawListener {

        private var previousContentViewHeight: Int = -1

        override fun onPreDraw(): Boolean {
            return detect().not()
        }

        private fun detect(): Boolean {
            val contentViewHeight = viewHolder.frameLayout.height

            if (contentViewHeight == previousContentViewHeight)
                return false

            if (previousContentViewHeight != -1) {
                val isKeyboardVisible = contentViewHeight < viewHolder.decorView.height - viewHolder.frameLayout.top

                onKeyboardVisibilityChanged(
                    KeyboardVisibilityChanged(
                        visible = isKeyboardVisible,
                        contentViewHeight = contentViewHeight,
                        previousContentViewHeight = previousContentViewHeight)
                )
            }

            previousContentViewHeight = contentViewHeight

            return true
        }
    }
}