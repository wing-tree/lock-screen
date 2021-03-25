package com.flow.android.kotlin.lockscreen.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.databinding.EventItemBinding

class EventAdapter(private val onItemClickListener: OnItemClickListener): ListAdapter<Event, EventAdapter.ViewHolder>(DiffCallback()) {

    interface OnItemClickListener {
        fun onItemClick(item: Event)
    }

    fun add(item: Event) {
        val list = currentList.toMutableList()
        list.add(item)
        submitList(list)
    }

    class ViewHolder private constructor(private val viewBinding: ViewBinding, private val onItemClickListener: OnItemClickListener):
            RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(item: Event) {
            viewBinding as EventItemBinding
            viewBinding.textCalendarDisplayName.text = item.calendarDisplayName
            viewBinding.textTitle.text = item.title

            viewBinding.root.setOnClickListener {
                onItemClickListener.onItemClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup, onItemClickListener: OnItemClickListener): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val viewBinding = EventItemBinding.inflate(inflater, parent, false)

                return ViewHolder(viewBinding, onItemClickListener)
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