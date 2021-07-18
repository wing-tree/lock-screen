package com.flow.android.kotlin.lockscreen.calendar.adapter

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.calendar.model.Model
import com.flow.android.kotlin.lockscreen.databinding.CalendarEventBinding
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import java.text.SimpleDateFormat
import java.util.*

class CalendarEventAdapter(private val onItemClick: (item: Model.CalendarEvent) -> Unit): ListAdapter<Model.CalendarEvent, CalendarEventAdapter.ViewHolder>(DiffCallback()) {
    val simpleDateFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())

    fun add(item: Model.CalendarEvent) {
        val list = currentList.toMutableList()
        list.add(item)
        submitList(list)
    }

    inner class ViewHolder(private val viewBinding: CalendarEventBinding) : RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(item: Model.CalendarEvent) {
            val begin = "${item.begin.format()} - "
            val fontSize = Preference.Display.getFontSize(viewBinding.root.context)

            viewBinding.textViewBegin.text = begin
            viewBinding.textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
            viewBinding.textViewTitle.text = item.title
            viewBinding.textViewEnd.text = item.end.format()
            viewBinding.viewCalendarColor.backgroundTintList = ColorStateList.valueOf(item.calendarColor)

            viewBinding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun Long.format() = simpleDateFormat.format(this)
    }

    private fun from(parent: ViewGroup): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = CalendarEventBinding.inflate(inflater, parent, false)

        return ViewHolder(viewBinding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DiffCallback: DiffUtil.ItemCallback<Model.CalendarEvent>() {
    override fun areItemsTheSame(oldItem: Model.CalendarEvent, newItem: Model.CalendarEvent): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Model.CalendarEvent, newItem: Model.CalendarEvent): Boolean {
        return oldItem == newItem
    }
}