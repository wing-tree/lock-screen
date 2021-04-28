package com.flow.android.kotlin.lockscreen.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.calendar.view.CalendarFragment
import com.flow.android.kotlin.lockscreen.favoriteapp.view.FavoriteAppsFragment
import com.flow.android.kotlin.lockscreen.memo.view.MemoFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    private val itemCount = 3

    override fun getItemCount(): Int = itemCount

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MemoFragment()
            1 -> CalendarFragment()
            2 -> FavoriteAppsFragment()
            else -> throw IllegalStateException("Invalid position.")
        }
    }
}