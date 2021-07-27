package com.flow.android.kotlin.lockscreen.calendar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.calendar.model.Model
import com.flow.android.kotlin.lockscreen.databinding.EventsBinding
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper

class EventsAdapter(private val onItemClick: (item: Model.Event) -> Unit)
    : ListAdapter<List<Model.Event>, EventsAdapter.ViewHolder>(DiffCallback()) {
    private var inflater: LayoutInflater? = null

    class ViewHolder private constructor(
            private val binding: EventsBinding,
            private val onItemClick: (item: Model.Event) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: List<Model.Event>) {
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManagerWrapper(binding.root.context)
                adapter = EventAdapter {
                    onItemClick(it)
                }.apply {
                    submitList(item)
                }

                scheduleLayoutAnimation()
            }
        }

        companion object {
            fun from(binding: EventsBinding, onItemClick: (item: Model.Event) -> Unit): ViewHolder {
                return ViewHolder(binding, onItemClick)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = this.inflater ?: LayoutInflater.from(parent.context)
        val binding = EventsBinding.inflate(inflater, parent, false)

        this.inflater = inflater

        return ViewHolder.from(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.count()

    private class DiffCallback: DiffUtil.ItemCallback<List<Model.Event>>() {
        override fun areItemsTheSame(oldItem: List<Model.Event>, newItem: List<Model.Event>): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: List<Model.Event>, newItem: List<Model.Event>): Boolean {
            return oldItem == newItem
        }
    }
}