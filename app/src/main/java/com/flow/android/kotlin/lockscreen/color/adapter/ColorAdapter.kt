package com.flow.android.kotlin.lockscreen.color.adapter

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.color.ColorCalculator
import com.flow.android.kotlin.lockscreen.databinding.ColorBinding
import com.flow.android.kotlin.lockscreen.util.*
import timber.log.Timber

class ColorAdapter(private val items: IntArray, private val onColorSelected: (color: Int) -> Unit) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
    class ViewHolder(val binding: ColorBinding) : RecyclerView.ViewHolder(binding.root)

    private var selectedPosition = -1
    private var inflater: LayoutInflater? = null
    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
        this.recyclerView?.itemAnimator = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = this.inflater ?: LayoutInflater.from(parent.context)

        this.inflater = inflater

        return ViewHolder(ColorBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = items[position]
        val dark = ContextCompat.getColor(holder.binding.root.context, R.color.dark_grey)
        val light = ContextCompat.getColor(holder.binding.root.context, R.color.white)

        holder.binding.imageView.setColorFilter(
                ColorCalculator.onBackgroundColor(color, dark, light, false),
                PorterDuff.Mode.SRC_ATOP
        )

        if (selectedPosition == position)
            holder.binding.imageView.show()
        else
            holder.binding.imageView.hide()

        holder.binding.frameLayout.backgroundTintList = ColorStateList.valueOf(color)

        holder.binding.frameLayout.setOnClickListener {
            if (it is FrameLayout) {
                if (selectedPosition == position)
                    return@setOnClickListener

                onColorSelected(color)
                notifyItemChanged(selectedPosition)
                notifyItemChanged(position)
                selectedPosition = position
            }
        }
    }

    override fun getItemCount(): Int = items.count()

    fun setInitiallySelectedColor(@ColorInt color: Int) {
        selectedPosition = items.indexOf(color)
        Timber.d("selectedPosition :$selectedPosition")
    }
}