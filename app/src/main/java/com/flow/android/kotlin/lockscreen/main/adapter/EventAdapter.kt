package com.flow.android.kotlin.lockscreen.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.databinding.EventItemBinding
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(private val onItemClickListener: OnItemClickListener): ListAdapter<Event, EventAdapter.ViewHolder>(DiffCallback()) {
    interface OnItemClickListener {
        fun onItemClick(item: Event)
    }

    fun add(item: Event) {
        val list = currentList.toMutableList()
        list.add(item)
        submitList(list)
    }

    class ViewHolder private constructor(
            private val viewBinding: ViewBinding,
            private val onItemClickListener: OnItemClickListener,
            private val simpleDateFormat: SimpleDateFormat
    ): RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(item: Event) {
            viewBinding as EventItemBinding

            val begin = "${item.begin.format()} - "

            viewBinding.textViewBegin.text = begin
            viewBinding.viewCalendarColor.setBackgroundColor(item.calendarColor)
            viewBinding.textViewCalendarDisplayName.text = item.calendarDisplayName
            viewBinding.textViewTitle.text = item.title
            viewBinding.textViewEnd.text = item.end.format()

            viewBinding.root.setOnClickListener {
                onItemClickListener.onItemClick(item)
            }
        }

        private fun Long.format() = simpleDateFormat.format(this)

        companion object {
            fun from(parent: ViewGroup, onItemClickListener: OnItemClickListener): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val simpleDateFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())
                val viewBinding = EventItemBinding.inflate(inflater, parent, false)

                return ViewHolder(viewBinding, onItemClickListener, simpleDateFormat)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, onItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

internal class DiffCallback: DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}