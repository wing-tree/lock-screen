package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flow.android.kotlin.lockscreen.databinding.ShortcutBinding
import com.flow.android.kotlin.lockscreen.shortcut.datamodel.App

class AppAdapter(private val onItemClick: (app: App) -> Unit): RecyclerView.Adapter<AppAdapter.ViewHolder>() {
    private val apps = arrayListOf<App>()
    private var layoutInflater: LayoutInflater? = null

    fun addAll(list: List<App>) {
        val positionStart = apps.count()
        apps.addAll(list)
        notifyItemRangeInserted(positionStart, list.count())
    }

    fun remove(app: App) {
        val index = apps.indexOf(app)

        if (index == -1)
            return

        apps.removeAt(index)
        notifyItemRemoved(index)
    }

    inner class ViewHolder(private val binding: ShortcutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: App) {
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
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.count()
}