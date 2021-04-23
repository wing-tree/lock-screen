package com.flow.android.kotlin.lockscreen.calendar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.databinding.EventsItemBinding

class EventsAdapter(private val eventsList: ArrayList<List<Event>>, private val onItemClick: (item: Event) -> Unit): RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    private var inflater: LayoutInflater? = null

    fun add(list: List<Event>) {
        eventsList.add(list)
        notifyItemInserted(itemCount.dec())
    }

    class ViewHolder private constructor(
            private val binding: EventsItemBinding,
            private val onItemClick: (item: Event) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: List<Event>) {
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = EventAdapter {
                    onItemClick(it)
                }.apply {
                    submitList(item)
                }
            }
        }

        companion object {
            fun from(binding: EventsItemBinding, onItemClick: (item: Event) -> Unit): ViewHolder {
                return ViewHolder(binding, onItemClick)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = this.inflater ?: LayoutInflater.from(parent.context)
        val binding = EventsItemBinding.inflate(inflater, parent, false)

        return ViewHolder.from(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventsList[position])
    }

    override fun getItemCount(): Int = eventsList.count()
}