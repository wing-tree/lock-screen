package com.flow.android.kotlin.lockscreen.calendar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.calendar.model.Model
import com.flow.android.kotlin.lockscreen.databinding.CalendarEventListBinding
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper

class CalendarEventListAdapter(private val currentList: ArrayList<List<Model.CalendarEvent>>, private val onItemClick: (item: Model.CalendarEvent) -> Unit): RecyclerView.Adapter<CalendarEventListAdapter.ViewHolder>() {
    private var inflater: LayoutInflater? = null

    fun add(list: List<Model.CalendarEvent>, notifyItemInserted: Boolean = true) {
        currentList.add(list)

        if (notifyItemInserted)
            notifyItemInserted(itemCount.dec())
    }

    fun clear() {
        currentList.clear()
        notifyDataSetChanged()
    }

    class ViewHolder private constructor(
            private val binding: CalendarEventListBinding,
            private val onItemClick: (item: Model.CalendarEvent) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: List<Model.CalendarEvent>) {
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManagerWrapper(binding.root.context)
                adapter = CalendarEventAdapter {
                    onItemClick(it)
                }.apply {
                    submitList(item)
                }

                scheduleLayoutAnimation()
            }
        }

        companion object {
            fun from(binding: CalendarEventListBinding, onItemClick: (item: Model.CalendarEvent) -> Unit): ViewHolder {
                return ViewHolder(binding, onItemClick)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = this.inflater ?: LayoutInflater.from(parent.context)
        val binding = CalendarEventListBinding.inflate(inflater, parent, false)

        this.inflater = inflater

        return ViewHolder.from(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.count()
}