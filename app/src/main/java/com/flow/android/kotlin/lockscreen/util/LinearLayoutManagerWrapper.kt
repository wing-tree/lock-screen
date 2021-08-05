package com.flow.android.kotlin.lockscreen.util

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import timber.log.Timber

class LinearLayoutManagerWrapper(context: Context, orientation: Int = RecyclerView.VERTICAL, reverseLayout: Boolean = false) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Timber.e(e)
        }
    }
}