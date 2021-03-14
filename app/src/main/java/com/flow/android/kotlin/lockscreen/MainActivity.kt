package com.flow.android.kotlin.lockscreen

import android.Manifest
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var viewBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        val bitmapDrawable = BitmapDrawable(resources, wallpaper())
                        viewBinding?.constraintLayout?.background = bitmapDrawable
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {

                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {

                    }
                }).check()
    }

    private fun wallpaper(): Bitmap? {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val wallpaperManager = WallpaperManager.getInstance(this)

            if (Build.VERSION.SDK_INT >= 24) {
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
}