package com.flow.android.kotlin.lockscreen.shortcut.adapter

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Point
import android.view.*
import android.view.View.DragShadowBuilder
import android.view.View.OnTouchListener
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

    private var x = 0
    private var y = 0

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
                ViewCompat.startDragAndDrop(binding.imageView, null, object : DragShadowBuilder(binding.imageView) {
                    override fun onProvideShadowMetrics(
                        outShadowSize: Point?,
                        outShadowTouchPoint: Point?
                    ) {
                        super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint)
                        outShadowTouchPoint?.set(x, y)
                    }

                    override fun onDrawShadow(canvas: Canvas?) {
                        canvas?.scale(0.875F, 0.875F)
                        super.onDrawShadow(canvas)
                    }
                }, item, 0)
                //view.startDrag()

                true
            }

            binding.root.setOnTouchListener(OnLongTouchListener())

            binding.root.setOnDragListener { v, event ->
                when(event.action) {
                    DragEvent.ACTION_DRAG_ENTERED -> {

                    }
                    DragEvent.ACTION_DRAG_EXITED -> {

                    }
                    DragEvent.ACTION_DRAG_ENDED -> {

                    }
                    DragEvent.ACTION_DROP -> {
                        onMove(adapterPosition, v.tag.toString().toInt())
                    }
                }

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
        val item = asyncListDiffer.currentList[position]

        holder.bind(item)
    }

    private inner class OnLongTouchListener : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = motionEvent.x.toInt()
                    y = motionEvent.y.toInt()

                    return false
                }
            }

            return false
        }
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