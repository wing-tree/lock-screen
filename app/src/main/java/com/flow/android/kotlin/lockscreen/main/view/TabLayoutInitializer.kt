package com.flow.android.kotlin.lockscreen.main.view

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.main.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.util.fadeIn
import com.flow.android.kotlin.lockscreen.widget.animateSelectedTab
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

object TabLayoutInitializer {
    private const val DURATION = 600L

    fun initialize(tabLayout: TabLayout, viewPager2: ViewPager2, adapter: FragmentStateAdapter) {
        val context = tabLayout.context

        val tabTexts = arrayOf(
                context.getString(R.string.main_activity_002),
                context.getString(R.string.main_activity_000),
                context.getString(R.string.main_activity_001),
                context.getString(R.string.main_activity_003)
        )

        viewPager2.offscreenPageLimit = 3
        viewPager2.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.customView = LayoutInflater.from(context).inflate(
                    R.layout.tab_custom_view,
                    tabLayout,
                    false
            )

            val textView = tab.customView?.findViewById<TextView>(R.id.text_view) ?: return@TabLayoutMediator

            textView.text = tabTexts[position]
        }.attach()

        val selectedTabIndex = Preference.getSelectedTabIndex(context)

        Handler(Looper.getMainLooper()).postDelayed({
            if (selectedTabIndex == 0)
                tabLayout.getTabAt(selectedTabIndex)?.animateSelectedTab()
            else
                tabLayout.getTabAt(selectedTabIndex)?.select()

            tabLayout.fadeIn(DURATION)
            viewPager2.fadeIn(DURATION)
        }, DURATION)
    }
}