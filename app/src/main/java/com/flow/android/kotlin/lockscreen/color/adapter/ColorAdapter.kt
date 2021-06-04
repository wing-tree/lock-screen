package com.flow.android.kotlin.lockscreen.color.adapter

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.color.ColorCalculator
import com.flow.android.kotlin.lockscreen.databinding.ColorBinding
import com.flow.android.kotlin.lockscreen.util.*

class ColorAdapter(private val items: IntArray, private val onColorSelected: (color: Int) -> Unit) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
    class ViewHolder(val binding: ColorBinding) : RecyclerView.ViewHolder(binding.root)

    private val duration = 50
    private var colorSelectedPosition = -1
    private var inflater: LayoutInflater? = null
    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = this.inflater ?: LayoutInflater.from(parent.context)

        this.inflater = inflater

        return ViewHolder(ColorBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = items[position]

        if (colorSelectedPosition == position)
            holder.binding.imageView.fadeIn(0)
        else
            holder.binding.imageView.fadeOut(duration)

        holder.binding.frameLayout.backgroundTintList = ColorStateList.valueOf(color)

        holder.binding.frameLayout.setOnClickListener {
            it as FrameLayout

            if (colorSelectedPosition == position)
                return@setOnClickListener

            val dark = ContextCompat.getColor(holder.binding.root.context, R.color.dark)
            val light = ContextCompat.getColor(holder.binding.root.context, R.color.light)

            holder.binding.imageView.setColorFilter(
                ColorCalculator.onBackgroundColor(color, dark, light),
                PorterDuff.Mode.SRC_ATOP
            )

            holder.binding.imageView.fadeIn(duration)
            onColorSelected(color)
            notifyItemChanged(colorSelectedPosition)
            colorSelectedPosition = position
        }
    }

    override fun getItemCount(): Int = items.count()
}