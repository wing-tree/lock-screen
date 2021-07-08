package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.graphics.Canvas
import androidx.annotation.MainThread
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import kotlin.math.abs

class ItemTouchCallback(
        private val adapter: ShortcutAdapter,
        @MainThread private val onExited: () -> Unit,
        private val clearView: () -> Unit
): ItemTouchHelper.Callback() {
    override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(
                 ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP,
                0
        )
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val width = viewHolder.itemView.width
        val height = viewHolder.itemView.height

        if ((width + height) / 4 < abs(dX) + abs(dY))
            onExited.invoke()
    }

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition

        adapter.onMove(from, to)

        return true
    }

    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        clearView.invoke()
    }
}