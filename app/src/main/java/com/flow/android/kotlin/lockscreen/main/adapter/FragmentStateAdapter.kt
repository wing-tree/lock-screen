package com.flow.android.kotlin.lockscreen.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.calendar.CalendarFragment
import com.flow.android.kotlin.lockscreen.favorite_app.view.FavoriteAppsFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    private val itemCount = 2

    override fun getItemCount(): Int = itemCount

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> CalendarFragment()
            1 -> FavoriteAppsFragment()
            else -> throw IllegalStateException("Invalid position.")
        }
    }
}