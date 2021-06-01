package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flow.android.kotlin.lockscreen.databinding.ShortcutBinding
import com.flow.android.kotlin.lockscreen.shortcut.entity.DisplayShortcut
import java.util.*

class DisplayShortcutAdapter(private val onItemClick: (displayShortcut: DisplayShortcut) -> Unit): RecyclerView.Adapter<DisplayShortcutAdapter.ViewHolder>() {
    private var layoutInflater: LayoutInflater? = null

    private val diffCallback = object : DiffUtil.ItemCallback<DisplayShortcut>() {
        override fun areItemsTheSame(oldItem: DisplayShortcut, newItem: DisplayShortcut): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: DisplayShortcut, newItem: DisplayShortcut): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<DisplayShortcut>) {
        asyncListDiffer.submitList(list)
    }

    inner class ViewHolder(private val binding: ShortcutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DisplayShortcut) {
            Glide.with(binding.root.context).load(item.icon).into(binding.imageView)
            binding.textView.text = item.label

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        return ViewHolder(ShortcutBinding.inflate(
                layoutInflater ?: LayoutInflater.from(parent.context),
                parent,
                false
        ))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = asyncListDiffer.currentList[position]

        holder.bind(app)
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.count()

    fun onMove(from: Int, to: Int) {
        val currentList = asyncListDiffer.currentList.toList()

        if (currentList.count() <= from || currentList.count() <= to)
            return

        val priority = asyncListDiffer.currentList[from].shortcut?.priority ?: System.currentTimeMillis()
        currentList[from].shortcut?.priority = currentList[to].shortcut?.priority ?: System.currentTimeMillis()
        currentList[to].shortcut?.priority = priority
        Collections.swap(currentList, from, to)

        submitList(currentList)
    }

    fun shortcuts() = asyncListDiffer.currentList.map { it.shortcut }
}