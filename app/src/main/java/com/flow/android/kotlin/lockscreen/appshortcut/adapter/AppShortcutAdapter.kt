package com.flow.android.kotlin.lockscreen.appshortcut.adapter

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.databinding.AppShortcutBinding
import com.flow.android.kotlin.lockscreen.appshortcut.model.Model
import java.util.*

class AppShortcutAdapter: RecyclerView.Adapter<AppShortcutAdapter.ViewHolder>() {
    private val currentList = arrayListOf<Model.AppShortcut>()

    private var layoutInflater: LayoutInflater? = null
    private var recyclerView: RecyclerView? = null

    interface Listener {
        fun onItemClick(item: Model.AppShortcut)
        fun onItemLongClick(view: View, item: Model.AppShortcut): Boolean
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

    fun add(item: Model.AppShortcut) {
        currentList.add(0, item)
        notifyItemInserted(0)
        recyclerView?.smoothScrollToPosition(0)
    }

    fun addAll(list: List<Model.AppShortcut>) {
        val positionStart = currentList.count()

        currentList.addAll(list)
        notifyItemRangeInserted(positionStart, list.count())
    }

    fun remove(item: Model.AppShortcut) {
        val index = currentList.indexOf(currentList.find { it.packageName == item.packageName })

        currentList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun update(item: Model.AppShortcut) {
        val index = currentList.indexOf(currentList.find { it.packageName == item.packageName })

        currentList[index] = item
        notifyItemChanged(index)
    }

    inner class ViewHolder(private val binding: AppShortcutBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: Model.AppShortcut) {
            binding.imageView.setImageDrawable(item.icon)
            binding.textView.text = item.label

            binding.root.setOnClickListener {
                listener?.onItemClick(item)
            }

            binding.root.setOnLongClickListener {
                listener?.onItemLongClick(binding.imageView, item) ?: return@setOnLongClickListener false
                true
            }
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        return ViewHolder(
            AppShortcutBinding.inflate(
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

    fun onItemMove(from: Int, to: Int) {
        if (currentList.count() <= from || currentList.count() <= to)
            return

        if (from < to) {
            for (i in from until to) {
                val priority = currentList[i].priority

                currentList[i].priority = currentList[i + 1].priority
                currentList[i + 1].priority = priority

                Collections.swap(currentList, i, i + 1)
                notifyItemMoved(i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                val priority = currentList[i].priority

                currentList[i].priority = currentList[i - 1].priority
                currentList[i - 1].priority = priority

                Collections.swap(currentList, i, i - 1)
                notifyItemMoved(i, i - 1)
            }
        }
    }

    fun currentList(): List<Model.AppShortcut> = currentList.toList()
}