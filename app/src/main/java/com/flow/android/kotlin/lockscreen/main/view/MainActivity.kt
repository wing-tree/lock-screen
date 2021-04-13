package com.flow.android.kotlin.lockscreen.main.view

import android.Manifest
import android.app.KeyguardManager
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View.GONE
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.configuration.view.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.flow.android.kotlin.lockscreen.lock_screen.LockScreenService
import com.flow.android.kotlin.lockscreen.main.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.main.torch.Torch
import com.flow.android.kotlin.lockscreen.main.view_model.MainViewModel
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.util.fadeIn
import com.flow.android.kotlin.lockscreen.util.scale
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException
import kotlin.math.pow


class MainActivity : AppCompatActivity() {

    private val torch: Torch by lazy {
        Torch(this)
    }
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this@MainActivity, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
        }).get(MainViewModel::class.java)
    }
    private var viewBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        if (ConfigurationPreferences.getShowOnLockScreen(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)

                val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
        }

        window?.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        checkManageOverlayPermission()
        checkPermission()

        initializeView()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            @Suppress("SpellCheckingInspection")
            when(requestCode) {
                CalendarHelper.RequestCode.EditEvent -> {
                    data ?: return

                    viewModel.calendarDisplays()?.let { calendarDisplays ->
                        CalendarHelper.events(contentResolver, calendarDisplays).also { events ->
                            events?.let { viewModel.submitEvents(it) }
                        }
                    }
                }
                CalendarHelper.RequestCode.InsertEvent -> {
                    data ?: return

                    viewModel.calendarDisplays()?.let { calendarDisplays ->
                        CalendarHelper.events(contentResolver, calendarDisplays).also { events ->
                            events?.let { viewModel.submitEvents(it) }
                        }
                    }
                }
            }
        }
    }

    private fun initializeView() {
        viewBinding?.let { viewBinding ->
            viewBinding.floatingActionButton.setOnClickListener {
                finish()
            }

            viewBinding.imageViewCamera.setOnClickListener {
                Dexter.withContext(this)
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                                    startActivity(intent)
                                }
                            }

                            override fun onPermissionDenied(response: PermissionDeniedResponse) {

                            }

                            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {

                            }
                        }).check()
            }

            viewBinding.imageViewSettings.setOnClickListener {
                ConfigurationFragment().apply {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_view, this, tag)
                            .addToBackStack(null)
                            .commit()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                viewBinding.imageViewHighlight.setOnClickListener {
                    torch.toggle()

                    val color = when(torch.mode) {
                        Torch.Mode.On -> ContextCompat.getColor(this, R.color.yellow_A_200)
                        Torch.Mode.Off -> ContextCompat.getColor(this, R.color.black)
                        else -> throw IllegalArgumentException("Invalid mode.")
                    }

                    (it as ImageView).setColorFilter(
                            color,
                            android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
            } else
                viewBinding.imageViewHighlight.visibility = GONE

            val tabTexts = arrayOf(
                    getString(R.string.main_activity_000),
                    getString(R.string.main_activity_001)
            )

            viewBinding.viewPager2.adapter = FragmentStateAdapter(this)
            TabLayoutMediator(viewBinding.centerAlignedTabLayout, viewBinding.viewPager2) { tab, position ->
                tab.tag = position
                tab.text = tabTexts[position]
            }.attach()

            viewBinding.centerAlignedTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    animateSelectedTab(tab)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    animateUnselectedTab(tab)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            viewBinding.centerAlignedTabLayout.getTabAt(0)?.select()
        }
    }

    private fun animateSelectedTab(tab: TabLayout.Tab) {
        tab.view.scale(1.4F)
    }

    private fun animateUnselectedTab(tab: TabLayout.Tab) {
        tab.view.scale(1.0F)
    }

    private fun setWallpaper() {
        wallpaper()?.let {
            viewBinding?.root?.background = it.toDrawable(resources)
            setTextColor(it)
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

    private fun checkManageOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // todo show alert message for permission. rational
                val uri = Uri.fromParts("package", packageName, null)
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                startActivityForResult(intent, 0)
            } else {
                val intent = Intent(applicationContext, LockScreenService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LockScreenService().enqueueWork(this, intent)
                    startForegroundService(intent)
                } else {
                    LockScreenService().enqueueWork(this, intent)
                    startService(intent)
                }
            }
        }
    }

    private fun checkPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        for (grantedPermissionResponse in report.grantedPermissionResponses) {
                            when (grantedPermissionResponse.permissionName) {
                                Manifest.permission.READ_CALENDAR -> {
                                    viewModel.postCalendarDisplays()
                                    viewModel.postEvents()
                                }
                                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                                    setWallpaper()
                                }
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) { /* ... */

                    }
                }).check()
    }
}