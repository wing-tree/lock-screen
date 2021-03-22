package com.flow.android.kotlin.lockscreen.main.view

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View.GONE
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.adapter.EventAdapter
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.flow.android.kotlin.lockscreen.main.torch.Torch
import com.flow.android.kotlin.lockscreen.main.view_model.MainViewModel
import kotlin.math.pow

class MainActivity : AppCompatActivity(), EventAdapter.OnItemClickListener {

    private val eventAdapter = EventAdapter(this)
    private val torch: Torch by lazy {
        Torch(this)
    }
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }
    private var viewBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        val wallpaper = wallpaper()

        setWallpaper(wallpaper)
        setTextColor(wallpaper.toBitmap())

        initializeAdapter()
        initializeLiveData()
        initializeView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            @Suppress("SpellCheckingInspection")
            when(requestCode) {
                CalendarHelper.RequestCode.EditEvent -> {
                    data?.let {
                        CalendarHelper.events(contentResolver, CalendarHelper.calendarDisplays(contentResolver)).also { events ->
                            eventAdapter.submitList(events)
                        }
                    }
                }
                CalendarHelper.RequestCode.InsertEvent -> {
                    data?.let {
                        CalendarHelper.events(contentResolver, CalendarHelper.calendarDisplays(contentResolver)).also { events ->
                            eventAdapter.submitList(events)
                        }
                    }
                }
            }
        }
    }

    private fun initializeAdapter() {
        viewBinding?.recyclerView?.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun initializeLiveData() {
        viewModel.events(contentResolver)

        viewModel.events.observe(this, { events ->
            eventAdapter.submitList(events)
        })
    }

    private fun initializeView() {
        viewBinding?.let { viewBinding ->
            viewBinding.appCompatImageView.setOnClickListener {
                CalendarHelper.insertEvent(this)
            }

            viewBinding.imageViewCamera.setOnClickListener {

            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                viewBinding.imageViewHighlight.setOnClickListener {
                    torch.toggle()
                }
            } else
                viewBinding.imageViewHighlight.visibility = GONE
        }
    }

    private fun setWallpaper(wallpaper: Drawable) {
        viewBinding?.root?.background = wallpaper
    }

    private fun wallpaper(): Drawable {
        val wallpaperManager = WallpaperManager.getInstance(this)

        return wallpaperManager.drawable
    }

    private fun setTextColor(bitmap: Bitmap) {
        val width = resources.getDimensionPixelSize(R.dimen.constraint_layout_date_time_width)
        val height = resources.getDimensionPixelSize(R.dimen.constraint_layout_date_time_height)

        Palette.Builder(bitmap).setRegion(0, 0, width, height).generate { palette ->
            val dominantColor = palette?.getDominantColor(Color.WHITE)
            val dateTimeTextColor = getDateTimeTextColor(dominantColor ?: Color.WHITE)

            viewBinding?.let {
                it.textClockDate.setTextColor(dateTimeTextColor)
                it.textClockTime.setTextColor(dateTimeTextColor)
            }
        }
    }

    @ColorInt
    private fun getDateTimeTextColor(@ColorInt colorInt: Int): Int {
        var red = Color.red(colorInt) / 255.0
        var green = Color.green(colorInt) / 255.0
        var blue = Color.blue(colorInt) / 255.0

        if (red <= 0.03928)
            red /= 12.92
        else
            red = ((red + 0.055) / 1.055).pow(2.4)

        if (green <= 0.03928)
            green /= 12.92
        else
            green = ((green + 0.055) / 1.055).pow(2.4)

        if (blue <= 0.03928)
            blue /= 12.92
        else
            blue = ((red + 0.055) / 1.055).pow(2.4)

        val y = 0.2126 * red + 0.7152 * green + 0.0722 * blue

        return if (y > 0.179)
            Color.BLACK
        else
            Color.WHITE
    }

    /** EventAdapter.OnItemClickListener */
    override fun onItemClick(item: Event) {
        CalendarHelper.editEvent(this, item)
    }
}