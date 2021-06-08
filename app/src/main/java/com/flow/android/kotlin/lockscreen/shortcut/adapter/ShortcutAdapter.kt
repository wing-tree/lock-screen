package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.graphics.Canvas
import android.graphics.Point
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import androidx.core.view.ViewCompat
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
    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

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

            binding.root.tag = adapterPosition

            binding.root.setOnClickListener {
                onItemClick(item)
            }

            binding.root.setOnLongClickListener {
                //onItemLongClick(it, item)

                val shadow = DragShadowBuilder(binding.imageView)
                ViewCompat.startDragAndDrop(binding.root, null, object: DragShadowBuilder(binding.imageView) {
                    override fun onProvideShadowMetrics(outShadowSize: Point?, outShadowTouchPoint: Point?) {
                        super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint)
                        val s = IntArray(2)
                        binding.imageView.getLocationOnScreen(s)
                        println("xxx: ${s[0]},,, yyy: ${s[1]}")
                        outShadowTouchPoint?.set(100, 0)
                        //super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint)
                    }

                    override fun onDrawShadow(canvas: Canvas?) {
                        binding.root.draw(canvas)
                        //super.onDrawShadow(canvas)
                    }
                }, item, 0)
                true
            }

            binding.root.setOnDragListener { v, event ->
                when(event.action) {
                    DragEvent.ACTION_DRAG_ENTERED -> { println("aaaaa: enter and view: $v,, ${event.clipData}") }
                    DragEvent.ACTION_DRAG_EXITED -> {println("aaaaa: exited and view: $v,, ${event.clipData}")}
                    DragEvent.ACTION_DRAG_ENDED -> {println("aaaaa: ended and view: $v,, ${event.clipData}")}
                    DragEvent.ACTION_DROP -> {
                        println("aaaaa: drop and view: $v,, ${event.clipData}")
                        // 여기서 v가 쓰레기통이면, 삭제.
                        println("aaaaaa: $adapterPosition,,, ${v.tag.toString()}")
                        onMove(adapterPosition, v.tag.toString().toInt())
                    }
                }

                true
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
        if (from == to)
            return

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