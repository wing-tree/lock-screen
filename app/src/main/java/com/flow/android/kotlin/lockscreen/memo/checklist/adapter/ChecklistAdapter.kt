package com.flow.android.kotlin.lockscreen.memo.checklist.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.ChecklistItemBinding
import com.flow.android.kotlin.lockscreen.persistence.entity.ChecklistItem
import com.flow.android.kotlin.lockscreen.util.hide
import com.flow.android.kotlin.lockscreen.util.show

class ChecklistAdapter(private val listener: Listener, private val isEditable: Boolean) :
        ListAdapter<ChecklistItem, ChecklistAdapter.ViewHolder>(DiffCallback())
{
    interface Listener {
        fun onCancelClick(item: ChecklistItem)
        fun onItemCheckedChange(item: ChecklistItem, isChecked: Boolean)
    }

    inner class ViewHolder(
        private val binding: ChecklistItemBinding,
        private val listener: Listener,
        private val isEditable: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChecklistItem) {
            val context = binding.root.context

            binding.textView.text = item.content

            if (item.isDone) {
                binding.textView.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    setTextColor(ContextCompat.getColor(context, R.color.disabled))
                }
            } else {
                binding.textView.apply {
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    setTextColor(ContextCompat.getColor(context, R.color.text))
                }
            }

            if (isEditable) {
                binding.checkBox.hide()
                binding.imageViewClear.show()

                binding.imageViewClear.setOnClickListener {
                    listener.onCancelClick(item)
                }
            } else {
                binding.checkBox.show()
                binding.imageViewClear.hide()

                binding.checkBox.apply {
                    setOnCheckedChangeListener(null)

                    isChecked = item.isDone

                    setOnCheckedChangeListener { _, isChecked ->
                        listener.onItemCheckedChange(item, isChecked)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                ChecklistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                listener,
                isEditable
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DiffCallback: DiffUtil.ItemCallback<ChecklistItem>() {
    override fun areItemsTheSame(oldItem: ChecklistItem, newItem: ChecklistItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChecklistItem, newItem: ChecklistItem): Boolean {
        return oldItem == newItem
    }
}