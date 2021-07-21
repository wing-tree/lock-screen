package com.flow.android.kotlin.lockscreen.memo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.PorterDuff
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.databinding.MemoBinding
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.util.*
import com.flow.android.kotlin.lockscreen.util.BLANK
import com.flow.android.kotlin.lockscreen.util.DEFAULT_FONT_SIZE
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MemoAdapter(private val listener: Listener) : RecyclerView.Adapter<MemoAdapter.ViewHolder>() {
    private val currentList = arrayListOf<Memo>()

    private var fontSize = MainApplication.instance?.let {
        Preference.Display.getFontSize(it)
    } ?: DEFAULT_FONT_SIZE

    private var timeFormat = MainApplication.instance?.let {
        Preference.Display.getTimeFormat(it)
    } ?: DEFAULT_TIME_FORMAT

    private var inflater: LayoutInflater? = null
    private var simpleDateFormat: SimpleDateFormat? = null

    interface Listener {
        fun onItemClick(item: Memo)
        fun onSwapIconTouch(viewHolder: ViewHolder)
    }

    fun refresh() {
        fontSize = MainApplication.instance?.let {
            Preference.Display.getFontSize(it)
        } ?: DEFAULT_FONT_SIZE

        timeFormat = MainApplication.instance?.let {
            Preference.Display.getTimeFormat(it)
        } ?: DEFAULT_TIME_FORMAT

        notifyDataSetChanged()
    }

    fun currentList() = currentList.toList()

    fun add(item: Memo) {
        currentList.add(0, item)
        notifyItemInserted(0)
    }

    fun remove(item: Memo) {
        val index = currentList.indexOf(currentList.find { it.id == item.id })

        currentList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun addAll(list: List<Memo>) {
        val positionStart = currentList.count()

        currentList.addAll(list)
        notifyItemRangeInserted(positionStart, list.count())
    }

    fun update(item: Memo) {
        val index = currentList.indexOf(currentList.find { it.id == item.id })

        currentList[index] = item
        notifyItemChanged(index)
    }

    fun onItemMove(from: Int, to: Int) {
        if (currentList.count() <= from || currentList.count() <= to)
            return

        val priority = currentList[from].priority

        currentList[from].priority = currentList[to].priority
        currentList[to].priority = priority

        Collections.swap(currentList, from, to)
        notifyItemMoved(to, from)

    }

    @ColorInt
    fun getColor(context: Context, @ColorRes id: Int) = ContextCompat.getColor(context, id)

    inner class ViewHolder(private val binding: MemoBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: Memo) {
            val context = binding.root.context
            val checklistCountText = if (item.checklist.isEmpty())
                BLANK
            else {
                val checklistCount = item.checklist.count()
                val checklistDoneCount = item.checklist.filter { it.isDone }.count()
                "$checklistDoneCount/$checklistCount"
            }

            binding.textViewContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)

            binding.textViewContent.text = item.content
            binding.textViewDate.text = item.modifiedTime.format(binding.root.context)

            if (item.isDone) {
                binding.textViewContent.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    setTextColor(getColor(context, R.color.disabled_light))
                    text = item.content
                }

                binding.textViewDate.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    setTextColor(getColor(context, R.color.disabled_light))
                    text = item.modifiedTime.format(binding.root.context)
                }

                binding.textViewChecklistCount.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    setTextColor(getColor(context, R.color.disabled_light))
                    text = checklistCountText
                }

                binding.viewColor.hide()
                binding.imageViewDone.show()
                binding.imageViewDone.setColorFilter(item.color, PorterDuff.Mode.SRC_IN)

                binding.root.setCardBackgroundColor(getColor(context, R.color.disabled_dark))
            } else {
                binding.textViewContent.apply {
                    text = item.content
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    setTextColor(getColor(context, R.color.white))
                }

                binding.textViewDate.apply {
                    text = item.modifiedTime.format(binding.root.context)
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    setTextColor(getColor(context, R.color.high_emphasis_light))
                }

                binding.textViewChecklistCount.apply {
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    setTextColor(getColor(context, R.color.high_emphasis_light))
                    text = checklistCountText
                }

                binding.imageViewDone.hide()
                binding.viewColor.show()
                binding.viewColor.backgroundTintList = ColorStateList.valueOf(item.color)

                binding.root.setCardBackgroundColor(getColor(context, R.color.card_background))
            }

            binding.imageViewSwap.setOnTouchListener { _, event ->
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        listener.onSwapIconTouch(this)
                        return@setOnTouchListener true
                    }
                }

                false
            }

            binding.root.setOnClickListener {
                listener.onItemClick(item)
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
        holder.bind(currentList[position])
    }

    private fun Long.format(context: Context): String {
        val simpleDateFormat = simpleDateFormat ?: SimpleDateFormat(timeFormat, Locale.getDefault())
        val todayTimeFormat = context.getString(R.string.format_time_000)

        if (DateUtils.isToday(this))
            simpleDateFormat.applyPattern(todayTimeFormat)
        else
            simpleDateFormat.applyPattern(timeFormat)

        return simpleDateFormat.format(this)
    }

    override fun getItemCount(): Int = currentList.count()
}