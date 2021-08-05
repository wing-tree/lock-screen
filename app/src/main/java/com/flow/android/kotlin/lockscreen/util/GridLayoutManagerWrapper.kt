package com.flow.android.kotlin.lockscreen.util

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager


class GridLayoutManagerWrapper : GridLayoutManager {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context?, spanCount: Int) : super(context, spanCount)
    constructor(context: Context?, spanCount: Int, orientation: Int, reverseLayout: Boolean) : super(context, spanCount, orientation, reverseLayout)
}