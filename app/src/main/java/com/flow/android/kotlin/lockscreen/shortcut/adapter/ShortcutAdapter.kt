package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.widget.RecyclerView
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

    fun add(item: Model.Shortcut) {
        currentList.add(0, item)
        notifyItemInserted(0)
    }

    fun addAll(list: List<Model.Shortcut>) {
        currentList.addAll(list)
        notifyDataSetChanged()
    }

    fun remove(item: Model.Shortcut) {
        val index = currentList.indexOf(currentList.find { it.packageName == item.packageName })

        currentList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun update(item: Model.Shortcut) {
        val index = currentList.indexOf(currentList.find { it.packageName == item.packageName })

        currentList[index] = item
        notifyItemChanged(index)
    }

    inner class ViewHolder(private val binding: ShortcutBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: Model.Shortcut) {
            //Glide.with(binding.root.context).load(item.icon).into(binding.imageView)
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

    fun currentList(): List<Model.Shortcut> = currentList.toList()
}