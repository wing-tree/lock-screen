package com.flow.android.kotlin.lockscreen.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.databinding.ItemEventBinding

class EventAdapter: ListAdapter<Event, EventAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder private constructor(val viewBinding: ViewBinding): RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(item: Event) {
            viewBinding as ItemEventBinding
            viewBinding.textCalendarDisplayName.text = item.calendarDisplayName
            viewBinding.textTitle.text = item.title
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val viewBinding = ItemEventBinding.inflate(inflater, parent, false)

                return ViewHolder(viewBinding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
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