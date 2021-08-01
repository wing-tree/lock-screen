package com.flow.android.kotlin.lockscreen.fluidcontentresize

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

object FluidContentResize {
    private var valueAnimator: ValueAnimator = ObjectAnimator()

    fun listen(activity: Activity) {
        val viewHolder = ActivityViewHolder.createFrom(activity)

        KeyboardVisibilityDetector.listen(viewHolder) {
            animate(viewHolder, it)
        }

        viewHolder.onDetach {
            valueAnimator.cancel()
            valueAnimator.removeAllUpdateListeners()
        }
    }

    private fun animate(viewHolder: ActivityViewHolder, event: KeyboardVisibilityChanged) {
        val contentView = viewHolder.contentView
        contentView.setHeight(event.previousContentViewHeight)

        valueAnimator.cancel()
        valueAnimator = ObjectAnimator.ofInt(event.previousContentViewHeight, event.contentViewHeight).apply {
            interpolator = FastOutSlowInInterpolator()
            duration = 300
        }
        valueAnimator.addUpdateListener { contentView.setHeight(it.animatedValue as Int) }
        valueAnimator.start()
    }

    private fun View.setHeight(height: Int) {
        val params = layoutParams
        params.height = height
        layoutParams = params
    }
}