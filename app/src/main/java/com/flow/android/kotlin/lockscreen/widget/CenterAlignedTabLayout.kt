package com.flow.android.kotlin.lockscreen.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.google.android.material.tabs.TabLayout


class CenterAlignedTabLayout : TabLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        val first = (getChildAt(0) as ViewGroup).getChildAt(0)
        val last = (getChildAt(0) as ViewGroup).getChildAt((getChildAt(0) as ViewGroup).childCount - 1)

        ViewCompat.setPaddingRelative(getChildAt(0), width / 2 - first.width / 2, 0, width / 2 - last.width / 2, 0)
    }
}