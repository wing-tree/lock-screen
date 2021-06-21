package com.flow.android.kotlin.lockscreen.color.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.color.adapter.ColorAdapter

class ColorPickerLayout : LinearLayout {
    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs)
    }

    private var onColorSelectedListener: OnColorSelectedListener? = null

    interface OnColorSelectedListener {
        fun onColorSelected(@ColorInt color: Int)
    }

    fun setOnColorSelectedListener(onColorSelectedListener: OnColorSelectedListener) {
        this.onColorSelectedListener = onColorSelectedListener
    }

    private val colors = resources.getIntArray(R.array.colors)
    private val colorAdapter = ColorAdapter(colors) {
        onColorSelectedListener?.onColorSelected(it)
    }

    private lateinit var recyclerView: RecyclerView

    private fun initialize(context: Context, attrs: AttributeSet? = null) {
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        recyclerView = RecyclerView(context)
        recyclerView.apply {
            this.layoutParams = layoutParams
            layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
            adapter = colorAdapter
        }

        addView(recyclerView)
    }

    fun select(color: Int) {
        colorAdapter.setInitiallySelectedColor(color)
    }
}