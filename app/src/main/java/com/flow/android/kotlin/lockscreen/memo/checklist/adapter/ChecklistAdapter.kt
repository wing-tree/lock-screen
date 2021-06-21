package com.flow.android.kotlin.lockscreen.memo.checklist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.databinding.ChecklistHeaderBinding
import com.flow.android.kotlin.lockscreen.databinding.ChecklistItemBinding
import com.flow.android.kotlin.lockscreen.persistence.entity.ChecklistItem
import com.flow.android.kotlin.lockscreen.util.hide
import com.flow.android.kotlin.lockscreen.util.show
import kotlinx.coroutines.*
import java.util.*
import kotlin.Comparator

class ChecklistAdapter(private val listener: Listener, private val isEditable: Boolean): ListAdapter<ChecklistItem, ChecklistAdapter.ViewHolder>(DiffCallback()) {
    interface Listener {
        fun onAddClick(content: String)
        fun onMoreClick(item: ChecklistItem)
        fun onItemCheckedChange(item: ChecklistItem, isChecked: Boolean)
    }

    class ViewHolder(
        private val binding: ChecklistItemBinding,
        private val listener: Listener,
        private val isEditable: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChecklistItem) {
            binding.editText.setText(item.content)

            if (isEditable) {
                binding.checkbox.hide()
                binding.imageViewMore.show()

                binding.imageViewMore.setOnClickListener {
                    listener.onMoreClick(item)
                }
            } else {
                binding.checkbox.show()
                binding.imageViewMore.hide()

                binding.checkbox.isChecked = item.done

                binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
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

    private fun sort(list: List<ChecklistItem>?) {
        list?.let {
            Collections.sort(list,
                Comparator { o1: ChecklistItem, o2: ChecklistItem ->
                    return@Comparator (o1.id - o2.id).toInt() // todo check.
                }
            )
        }
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