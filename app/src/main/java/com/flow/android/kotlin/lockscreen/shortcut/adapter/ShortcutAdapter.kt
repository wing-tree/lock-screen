package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flow.android.kotlin.lockscreen.databinding.ShortcutBinding
import com.flow.android.kotlin.lockscreen.shortcut.model.Model

import java.util.*

class ShortcutAdapter: RecyclerView.Adapter<ShortcutAdapter.ViewHolder>() {
    private val currentList = arrayListOf<Model.Shortcut>()

    private var layoutInflater: LayoutInflater? = null
    private var recyclerView: RecyclerView? = null

    interface Listener {
        fun onItemClick(item: Model.Shortcut)
        fun onItemLongClick(view: View, item: Model.Shortcut): Boolean
    }

    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    fun addAll(list: List<Model.Shortcut>) {
        val positionStart = currentList.count()

        currentList.addAll(list)
        notifyItemRangeInserted(positionStart, list.count())
    }

    fun remove(item: Model.Shortcut) {
        val index = currentList.indexOf(currentList.find { it.packageName == item.packageName })

        currentList.removeAt(index)
        notifyItemRemoved(index)
    }

    inner class ViewHolder(private val binding: ShortcutBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: Model.Shortcut) {
            Glide.with(binding.root.context).load(item.icon).into(binding.imageView)
            binding.root.tag = adapterPosition
            binding.textView.text = item.label

            binding.root.setOnClickListener {
                listener?.onItemClick(item)
            }

            binding.root.setOnLongClickListener {
                listener?.onItemLongClick(it, item) ?: return@setOnLongClickListener false
                true
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        return ViewHolder(
            ShortcutBinding.inflate(
                layoutInflater ?: LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int = currentList.count()

    fun onMove(from: Int, to: Int) {
        if (currentList.count() <= from || currentList.count() <= to)
            return

        val priority = currentList[from].priority

        currentList[from].priority = currentList[to].priority
        currentList[to].priority = priority

        Collections.swap(currentList, from, to)
        notifyItemMoved(from, to)
    }

    fun currentList(): List<Model.Shortcut> = currentList.toList()
}