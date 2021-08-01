package com.flow.android.kotlin.lockscreen.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.flow.android.kotlin.lockscreen.util.scale
import com.google.android.material.tabs.TabLayout

class CenterAlignedTabLayout : TabLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab) {
                tab.animateSelectedTab()
            }

            override fun onTabUnselected(tab: Tab) {
                tab.animateUnselectedTab()
            }

            override fun onTabReselected(tab: Tab) {}
        })
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        val first = (getChildAt(0) as ViewGroup).getChildAt(0)
        val last = (getChildAt(0) as ViewGroup).getChildAt((getChildAt(0) as ViewGroup).childCount - 1)

        ViewCompat.setPaddingRelative(getChildAt(0), width / 2 - first.width / 2, 0, width / 2 - last.width / 2, 0)
    }
}

fun TabLayout.Tab.animateSelectedTab() = run { view.scale(1.5F) }
fun TabLayout.Tab.animateUnselectedTab() = run { view.scale(1.0F) }