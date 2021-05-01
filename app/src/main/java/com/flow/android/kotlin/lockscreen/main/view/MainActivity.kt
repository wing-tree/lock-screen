package com.flow.android.kotlin.lockscreen.main.view

import android.Manifest
import android.app.KeyguardManager
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.color.ColorHelper
import com.flow.android.kotlin.lockscreen.configuration.view.ConfigurationFragment
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.flow.android.kotlin.lockscreen.lockscreen.LockScreenService
import com.flow.android.kotlin.lockscreen.main.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.home.homewatcher.HomeWatcher
import com.flow.android.kotlin.lockscreen.home.homewatcher.HomePressedListener
import com.flow.android.kotlin.lockscreen.main.torch.Torch
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel
import com.flow.android.kotlin.lockscreen.main.viewmodel.MemoChanged
import com.flow.android.kotlin.lockscreen.main.viewmodel.MemoChangedState
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import com.flow.android.kotlin.lockscreen.memo.view.MemoDetailDialogFragment
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
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

class MainActivity : AppCompatActivity(), MemoDetailDialogFragment.OnMemoChangedListener {
    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private val torch: Torch by lazy {
        Torch(this)
    }

    private val colorHelper: ColorHelper by lazy {
        ColorHelper.getInstance(application)
    }

    private val viewModel: MainViewModel by viewModels()
    private var _viewBinding: ActivityMainBinding? = null
    private val viewBinding: ActivityMainBinding
        get() = _viewBinding!!

    private val homeWatcher = HomeWatcher(this).apply {
        setOnHomePressedListener(object : HomePressedListener {
            override fun onHomeKeyPressed() {
                localBroadcastManager.sendBroadcastSync(Intent(LockScreenService.Action.HomeKeyPressed))
                finish()
            }

            override fun onRecentAppsPressed() {
                localBroadcastManager.sendBroadcastSync(Intent(LockScreenService.Action.RecentAppsPressed))
                finish()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (ConfigurationPreferences.getShowOnLockScreen(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager

                keyguardManager.requestDismissKeyguard(this, null)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                )
            }
        }

        window?.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        checkManageOverlayPermission()
        checkPermission()

        initializeLiveData()
        initializeView()
        firstRun()
    }

    override fun onBackPressed() {
        if (onBackPressedDispatcher.hasEnabledCallbacks())
            onBackPressedDispatcher.onBackPressed()
        else
            viewBinding.floatingActionButton.forceRippleAnimation()
    }

    override fun onStart() {
        super.onStart()
        homeWatcher.startWatch()
    }

    override fun onPause() {
        val index = viewBinding.centerAlignedTabLayout.selectedTabPosition

        ConfigurationPreferences.putSelectedTabIndex(this, index)

        super.onPause()
    }

    override fun onStop() {
        homeWatcher.stopWatch()
        super.onStop()
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
//                        CalendarHelper.events(contentResolver, calendarDisplays).also { events ->
//                            events?.let { viewModel.submitEvents(it) }
//                        }
                    }
                }
                CalendarHelper.RequestCode.InsertEvent -> {
                    data ?: return

                    viewModel.calendarDisplays()?.let { calendarDisplays ->
//                        CalendarHelper.events(contentResolver, calendarDisplays).also { events ->
//                            events?.let { viewModel.submitEvents(it) }
//                        }
                    }
                }
            }
        }
    }

    private fun initializeLiveData() {
        viewModel.floatingActionButtonVisibility.observe(this, { visibility ->
            if (visibility == VISIBLE)
                viewBinding.floatingActionButton.show()
            else if (visibility == GONE)
                viewBinding.floatingActionButton.hide()
        })


    }

    private fun initializeView() {
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

                    override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest?,
                            token: PermissionToken?
                    ) {

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
                getString(R.string.main_activity_002),
                getString(R.string.main_activity_000),
                getString(R.string.main_activity_001)
        )

        viewBinding.viewPager2.adapter = FragmentStateAdapter(this)
        TabLayoutMediator(viewBinding.centerAlignedTabLayout, viewBinding.viewPager2) { tab, position ->
            tab.customView = layoutInflater.inflate(
                    R.layout.tab_custom_view,
                    viewBinding.root,
                    false
            )

            tab.customView?.findViewById<TextView>(R.id.text_view)?.text = tabTexts[position]
        }.attach()

        viewBinding.centerAlignedTabLayout.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                animateSelectedTab(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                animateUnselectedTab(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val selectedTabIndex = ConfigurationPreferences.getSelectedTabIndex(this)

        viewBinding.centerAlignedTabLayout.getTabAt(selectedTabIndex)?.select()
    }

    private fun setTabTextColor(bitmap: Bitmap) {
        viewBinding.centerAlignedTabLayout.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val bottom = viewBinding.centerAlignedTabLayout.bottom
                val left = viewBinding.centerAlignedTabLayout.left
                val right = viewBinding.centerAlignedTabLayout.right
                val top = viewBinding.centerAlignedTabLayout.top

                Palette.Builder(bitmap).setRegion(left, top, right, bottom).generate { palette ->
                    val dominantColor = palette?.getDominantColor(Color.WHITE)
                    val textColor = colorHelper.colorDependingOnBackground(dominantColor ?: Color.WHITE)

                    val tabCount = viewBinding.centerAlignedTabLayout.tabCount

                    for (i in 0 until tabCount) {
                        val tab = viewBinding.centerAlignedTabLayout.getTabAt(i) ?: continue

                        tab.customView?.findViewById<TextView>(R.id.text_view)?.setTextColor(textColor)
                    }
                }

                viewBinding.centerAlignedTabLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun animateSelectedTab(tab: TabLayout.Tab) {
        tab.view.scale(1.5F)
    }

    private fun animateUnselectedTab(tab: TabLayout.Tab) {
        tab.view.scale(1.0F)
    }

    private fun setWallpaper() {
        wallpaper()?.let {
            colorHelper.setViewPagerRegionColor(viewBinding.viewPager2, it)
            viewBinding.root.background = it.toDrawable(resources)
            viewModel.setWallpaper(it)
            setTextColor(it)
            setTabTextColor(it)
        }
    }

    private fun wallpaper(): Bitmap? {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val wallpaperManager = WallpaperManager.getInstance(this)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val wallpaperFile = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK) ?: wallpaperManager.getWallpaperFile(
                        WallpaperManager.FLAG_SYSTEM
                )

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
        colorHelper.setTextColor(viewBinding.textClockDate, bitmap)
        colorHelper.setTextColor(viewBinding.textClockTime, bitmap)
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundService(intent)
                else
                    startService(intent)
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
                                    // viewModel.postEvents()
                                }
                                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                                    setWallpaper()
                                }
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest>,
                            token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    private fun firstRun() {
        if (ConfigurationPreferences.getFirstRun(this)) {
            viewModel.insertMemo(
                Memo(
                    content = getString(R.string.memo_fragment_000),
                    modifiedTime = System.currentTimeMillis(),
                    priority = System.currentTimeMillis()
                )
            )
            ConfigurationPreferences.putFirstRun(this, false)
        }
    }

    private fun View.forceRippleAnimation() {
        if (background is RippleDrawable) {
            val handler = Handler(Looper.getMainLooper())
            val rippleDrawable = background

            rippleDrawable.state = intArrayOf(
                    android.R.attr.state_pressed,
                    android.R.attr.state_enabled
            )
            handler.postDelayed({ rippleDrawable.state = intArrayOf() }, 200)
        }
    }

    override fun onMemoDeleted(memo: Memo) {
        viewModel.notifyMemoChanged(MemoChanged(memo, MemoChangedState.Deleted))
    }

    override fun onMemoDone(memo: Memo) {
        viewModel.notifyMemoChanged(MemoChanged(memo, MemoChangedState.Modified))
    }
}