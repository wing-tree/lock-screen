package com.flow.android.kotlin.lockscreen

import android.Manifest
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.adapter.EventAdapter
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException
import kotlin.math.pow

internal const val BLANK = ""

class MainActivity : AppCompatActivity(), EventAdapter.OnItemClickListener {

    private val eventAdapter = EventAdapter(this)
    private var viewBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        Dexter.withContext(this)
                .withPermission(Manifest.permission.GET_ACCOUNTS)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        val wallpaper = wallpaper()
                        test(wallpaper!!)
                        val bitmapDrawable = BitmapDrawable(resources, wallpaper)
                        viewBinding?.root?.background = bitmapDrawable
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {

                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {

                    }
                }).check()

        viewBinding?.appCompatImageButton?.setOnClickListener {
            CalendarHelper.insertEvent(this)
        }

        viewBinding?.recyclerView?.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        val events = CalendarHelper.events(contentResolver, CalendarHelper.calendarDisplays(contentResolver))
        eventAdapter.submitList(events)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        println("INTHEEEEEEEEEE $resultCode,, $RESULT_OK,,, $RESULT_CANCELED")

        if (resultCode == RESULT_OK) {
            println("RESULT_OK OOOOOOOOO")
            @Suppress("SpellCheckingInspection")
            when(requestCode) {
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

    private fun wallpaper(): Bitmap? {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val wallpaperManager = WallpaperManager.getInstance(this)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val wallpaperFile = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK) ?: wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)

                wallpaperFile?.let {
                    val bitmap = BitmapFactory.decodeFileDescriptor(wallpaperFile.fileDescriptor)

                    try {
                        wallpaperFile.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    return bitmap
                }
            }

            return wallpaperManager.drawable.toBitmap()
        }

        return null
    }

    private fun test(bitmap: Bitmap) {
        val p: Palette = Palette.from(bitmap).generate()

        viewBinding?.constraintLayout?.post {

            val width = viewBinding?.constraintLayout?.width!!
            val height = viewBinding?.constraintLayout?.height!!

            println("WIDTH: $width")
            println("HEIGHT: $width")

            val bb = Palette.Builder(bitmap).setRegion(0, 0, width, height).generate { palette ->
                val domin = palette?.getDominantColor(Color.WHITE)
                println("domin1: $domin")
                getDateTimeTextColor(domin ?: Color.WHITE)
            }

            val cc = Palette.Builder(bitmap).generate { palette ->
                val domin = palette?.getDominantColor(Color.WHITE)
                println("domin2: $domin")
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