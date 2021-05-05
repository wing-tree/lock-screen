package com.flow.android.kotlin.lockscreen.memo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.databinding.MemoBinding
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MemoAdapter(
    private val items: ArrayList<Memo>,
    private val onItemClick: (item: Memo) -> Unit,
    private val onSwapTouch: (viewHolder: ViewHolder) -> Unit
) : RecyclerView.Adapter<MemoAdapter.ViewHolder>() {
    private var inflater: LayoutInflater? = null
    private var simpleDateFormat: SimpleDateFormat? = null

    inner class ViewHolder(private val binding: MemoBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: Memo) {
            binding.textViewContent.text = item.content
            binding.textViewDate.text = item.modifiedTime.format(binding.root.context)

            binding.imageViewSwap.setOnTouchListener { _, event ->
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onSwapTouch(this)
                        return@setOnTouchListener true
                    }
                }

                false
            }


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
        holder.bind(items[position])
    }

    fun add(item: Memo) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    fun remove(item: Memo) {
        val index = items.indexOf(items.find { it.id == item.id })

        items.removeAt(index)
        notifyItemRemoved(index)
    }

    // todo deep copy 문제 발생가능함 응 발생함 ㅅㅂ.
    fun change(item: Memo) {
        val index = items.indexOf(items.find { it.id == item.id })

        println("ZIO XIAL: $index")
        println("ZIO XIAL22: $item")
        println("ZIO XIAL333: ${items[index]}")

        items[index] = item
        notifyItemChanged(index)
    }

    fun addAll(list: List<Memo>) {
        val positionStart = items.count()

        items.addAll(list)
        notifyItemRangeInserted(positionStart, list.count())
    }

    fun onMove(from: Int, to: Int) {
        val priority = items[from].priority
        items[from].priority = items[to].priority
        items[to].priority = priority
        Collections.swap(items, from, to)

        notifyItemMoved(from, to)
    }

    private fun Long.format(context: Context): String {
        val pattern = context.getString(R.string.format_date_001)
        val simpleDateFormat = simpleDateFormat ?: SimpleDateFormat(pattern, Locale.getDefault())

        return simpleDateFormat.format(this)
    }

    override fun getItemCount(): Int = items.count()
}