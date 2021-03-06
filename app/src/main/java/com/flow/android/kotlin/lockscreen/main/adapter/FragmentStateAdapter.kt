package com.flow.android.kotlin.lockscreen.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.calendar.view.CalendarFragment
import com.flow.android.kotlin.lockscreen.note.view.NoteFragment
import com.flow.android.kotlin.lockscreen.appshortcut.view.AppShortcutFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    private val itemCount = 3

    override fun getItemCount(): Int = itemCount

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> NoteFragment()
            1 -> CalendarFragment()
            2 -> AppShortcutFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}