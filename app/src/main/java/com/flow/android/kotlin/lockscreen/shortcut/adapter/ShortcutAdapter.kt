package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flow.android.kotlin.lockscreen.databinding.ShortcutBinding
import com.flow.android.kotlin.lockscreen.shortcut.model.ShortcutModel
import java.util.*

class ShortcutAdapter(
        private val onItemClick: (item: ShortcutModel) -> Unit,
        private val onItemLongClick: (view: View, item: ShortcutModel) -> Boolean,
): RecyclerView.Adapter<ShortcutAdapter.ViewHolder>() {
    private var layoutInflater: LayoutInflater? = null

    private val diffCallback = object : DiffUtil.ItemCallback<ShortcutModel>() {
        override fun areItemsTheSame(oldItem: ShortcutModel, newItem: ShortcutModel): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: ShortcutModel, newItem: ShortcutModel): Boolean {
            return oldItem == newItem
        }
    }

    fun addAll(list: List<ShortcutModel>) {
        val currentList = asyncListDiffer.currentList.toMutableList()

        currentList.addAll(list)
        submitList(currentList)
    }

    fun remove(item: ShortcutModel) {
        val currentList = asyncListDiffer.currentList.toMutableList()

        currentList.remove(item)
        submitList(currentList)
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<ShortcutModel>) {
        asyncListDiffer.submitList(list)
    }

    inner class ViewHolder(private val binding: ShortcutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShortcutModel) {
            Glide.with(binding.root.context).load(item.icon).into(binding.imageView)
            binding.textView.text = item.label

            binding.root.setOnClickListener {
                onItemClick(item)
            }

            binding.root.setOnLongClickListener {
                onItemLongClick(it, item)
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
        val item = asyncListDiffer.currentList[position]

        holder.bind(item)
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.count()

    fun onMove(from: Int, to: Int) {
        val currentList = asyncListDiffer.currentList.toList()

        if (currentList.count() <= from || currentList.count() <= to)
            return

        val priority = asyncListDiffer.currentList[from]?.priority ?: System.currentTimeMillis()
        currentList[from]?.priority = currentList[to]?.priority ?: System.currentTimeMillis()
        currentList[to]?.priority = priority
        Collections.swap(currentList, from, to)

        submitList(currentList)
    }

    fun currentList(): List<ShortcutModel> = asyncListDiffer.currentList
}