package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flow.android.kotlin.lockscreen.databinding.ShortcutBinding
import com.flow.android.kotlin.lockscreen.shortcut.datamodel.ShortcutDataModel
import java.util.*

class ShortcutAdapter(private val onItemClick: (item: ShortcutDataModel) -> Unit): RecyclerView.Adapter<ShortcutAdapter.ViewHolder>() {
    private var layoutInflater: LayoutInflater? = null

    private val diffCallback = object : DiffUtil.ItemCallback<ShortcutDataModel>() {
        override fun areItemsTheSame(oldItem: ShortcutDataModel, newItem: ShortcutDataModel): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: ShortcutDataModel, newItem: ShortcutDataModel): Boolean {
            return oldItem == newItem
        }
    }

    fun addAll(list: List<ShortcutDataModel>) {
        val currentList = asyncListDiffer.currentList.toMutableList()

        currentList.addAll(list)
        submitList(currentList)
    }

    fun remove(item: ShortcutDataModel) {
        val currentList = asyncListDiffer.currentList.toMutableList()

        currentList.remove(item)
        submitList(currentList)
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<ShortcutDataModel>) {
        asyncListDiffer.submitList(list)
    }

    inner class ViewHolder(private val binding: ShortcutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShortcutDataModel) {
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

        val priority = asyncListDiffer.currentList[from]?.priority ?: System.currentTimeMillis()
        currentList[from]?.priority = currentList[to]?.priority ?: System.currentTimeMillis()
        currentList[to]?.priority = priority
        Collections.swap(currentList, from, to)

        submitList(currentList)
    }

    fun currentList(): List<ShortcutDataModel> = asyncListDiffer.currentList
}