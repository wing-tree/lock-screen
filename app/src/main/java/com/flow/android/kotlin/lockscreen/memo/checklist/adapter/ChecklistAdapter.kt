package com.flow.android.kotlin.lockscreen.memo.checklist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.databinding.ChecklistItemBinding
import com.flow.android.kotlin.lockscreen.persistence.entity.ChecklistItem
import com.flow.android.kotlin.lockscreen.util.hide
import com.flow.android.kotlin.lockscreen.util.show

class ChecklistAdapter(private val listener: Listener, private val isEditable: Boolean): ListAdapter<ChecklistItem, ChecklistAdapter.ViewHolder>(DiffCallback()) {
    interface Listener {
        fun onCancelClick(item: ChecklistItem)
        fun onItemCheckedChange(item: ChecklistItem, isChecked: Boolean)
    }

    class ViewHolder(
        private val binding: ChecklistItemBinding,
        private val listener: Listener,
        private val isEditable: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChecklistItem) {
            binding.editText.text = item.content

            if (isEditable) {
                binding.checkBox.hide()
                binding.imageViewClear.show()

                binding.imageViewClear.setOnClickListener {
                    listener.onCancelClick(item)
                }
            } else {
                binding.checkBox.show()
                binding.imageViewClear.hide()

                binding.checkBox.isChecked = item.isDone

                binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                    listener.onItemCheckedChange(item, isChecked)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, listener: Listener, isEditable: Boolean): ViewHolder {
                return ViewHolder(
                    ChecklistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    listener,
                    isEditable
                )
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, listener, isEditable)
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