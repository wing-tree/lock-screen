package com.flow.android.kotlin.lockscreen.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.calendar.view.CalendarFragment
import com.flow.android.kotlin.lockscreen.shortcut.view.ShortcutFragment
import com.flow.android.kotlin.lockscreen.memo.view.MemoFragment
import com.flow.android.kotlin.lockscreen.notification.view.NotificationFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    private val itemCount = 4

    override fun getItemCount(): Int = itemCount

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MemoFragment()
            1 -> CalendarFragment()
            2 -> ShortcutFragment()
            3 -> NotificationFragment()
            else -> throw IllegalStateException("Invalid position.")
        }
    }
}