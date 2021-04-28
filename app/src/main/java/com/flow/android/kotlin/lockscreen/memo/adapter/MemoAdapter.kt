package com.flow.android.kotlin.lockscreen.memo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.MemoBinding
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import java.text.SimpleDateFormat
import java.util.*

class MemoAdapter(private val onItemClick: (item: Memo) -> Unit): ListAdapter<Memo, MemoAdapter.ViewHolder>(DiffCallback()) {
    private var inflater: LayoutInflater? = null
    private var simpleDateFormat: SimpleDateFormat? = null

    inner class ViewHolder(private val binding: MemoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Memo) {
            binding.textViewContent.text = item.content
            binding.textViewDate.text = item.modifiedTime.format(binding.root.context)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        val inflater = this.inflater ?: LayoutInflater.from(parent.context)
        val binding = MemoBinding.inflate(inflater, parent, false)

        this.inflater = inflater

        return ViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun Long.format(context: Context): String {
        val pattern = context.getString(R.string.format_date_001)
        val simpleDateFormat = simpleDateFormat ?: SimpleDateFormat(pattern, Locale.getDefault())

        return simpleDateFormat.format(this)
    }
}

class DiffCallback: DiffUtil.ItemCallback<Memo>() {
    override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean {
        return oldItem == newItem
    }
}