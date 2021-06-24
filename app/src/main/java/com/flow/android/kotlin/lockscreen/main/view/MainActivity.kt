package com.flow.android.kotlin.lockscreen.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Point
import android.graphics.PorterDuff
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.view.MotionEvent
import android.view.View.GONE
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.application.ApplicationUtil
import com.flow.android.kotlin.lockscreen.application.ApplicationUtil.getApplicationLabel
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.configuration.view.ConfigurationActivity
import com.flow.android.kotlin.lockscreen.configuration.viewmodel.ConfigurationChange
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.flow.android.kotlin.lockscreen.home.homewatcher.HomePressedListener
import com.flow.android.kotlin.lockscreen.home.homewatcher.HomeWatcher
import com.flow.android.kotlin.lockscreen.lockscreen.service.LockScreenService
import com.flow.android.kotlin.lockscreen.main.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.main.torch.Torch
import com.flow.android.kotlin.lockscreen.main.view.MainActivity.OpenLock.endRange
import com.flow.android.kotlin.lockscreen.main.view.MainActivity.OpenLock.outOfEndRange
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel
import com.flow.android.kotlin.lockscreen.memo._interface.OnMemoChangedListener
import com.flow.android.kotlin.lockscreen.notification.model.NotificationModel
import com.flow.android.kotlin.lockscreen.notification.service.NotificationListener
import com.flow.android.kotlin.lockscreen.permission._interface.OnPermissionAllowClickListener
import com.flow.android.kotlin.lockscreen.permission.view.PermissionRationaleDialogFragment
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.util.*
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
import timber.log.Timber
import java.io.File
import java.lang.Math.pow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), OnMemoChangedListener, OnPermissionAllowClickListener {
    private val duration = 500L
    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private val torch: Torch by lazy {
        Torch(this)
    }

    private val viewModel: MainViewModel by viewModels()
    private var _viewBinding: ActivityMainBinding? = null
    private val viewBinding: ActivityMainBinding
        get() = _viewBinding!!

    private val homeWatcher = HomeWatcher(this).apply {
        setOnHomePressedListener(object : HomePressedListener {
            override fun onHomeKeyPressed() {
                if (isFinishing)
                    return

                localBroadcastManager.sendBroadcastSync(Intent(LockScreenService.Action.HomeKeyPressed))
                finish()
            }

            override fun onRecentAppsPressed() {
                if (isFinishing)
                    return

                localBroadcastManager.sendBroadcastSync(Intent(LockScreenService.Action.RecentAppsPressed))
                finish()
            }
        })
    }

    private val notificationListenerConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is NotificationListener.NotificationListenerBinder) {
                val notificationListener = service.getNotificationListener()
                val activeNotifications = notificationListener.activeNotifications.mapNotNull {
                    it?.let { sbn -> NotificationModel(
                            label = getApplicationLabel(packageManager, sbn.packageName),
                            notification = sbn.notification,
                            postTime = sbn.postTime
                    ) }
                }

                viewModel.setNotifications(activeNotifications)
            } else {
                // call event for error. todo.
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // todo show error.
        }
    }

    private val delayMillis = 1000L
    private val handler by lazy { Handler(mainLooper) }
    private val checkManageOverlayPermission: Runnable = object : Runnable {
        @TargetApi(23)
        override fun run() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return

            if (Settings.canDrawOverlays(this@MainActivity)) {
                val intent = Intent(this@MainActivity, MainActivity::class.java)

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)

                return
            }

            handler.postDelayed(this, delayMillis)
        }
    }

    private object OpenLock {
        var x = 0F
        var y = 0F
        const val endRange = 600F
        var outOfEndRange = false
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionRationaleDialogFragment.permissionsGranted(this).not()) {
                PermissionRationaleDialogFragment().also {
                    it.show(supportFragmentManager, it.tag)
                }
            } else {
                startService()
                viewModel.postCalendarDisplays()
            }
        } else {
            startService()
            viewModel.postCalendarDisplays()
        }

        initializeView()
        firstRun()
    }

    override fun onBackPressed() {
        if (onBackPressedDispatcher.hasEnabledCallbacks())
            onBackPressedDispatcher.onBackPressed()
        else
            viewBinding.frameLayoutRipple.forceRippleAnimation()
    }

    override fun onStart() {
        super.onStart()
        bindNotificationListener()
        homeWatcher.startWatch()
    }

    override fun onPause() {
        with(viewBinding.centerAlignedTabLayout.selectedTabPosition) {
            ConfigurationPreferences.putSelectedTabIndex(this@MainActivity, this)
        }

        super.onPause()
    }

    override fun onStop() {
        homeWatcher.stopWatch()
        unbindNotificationListener()
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
                CalendarLoader.RequestCode.EditEvent -> {
                    data ?: return

                    viewModel.callRefreshEvents()

//                    viewModel.calendarDisplays()?.let { calendarDisplays ->
//                        CalendarHelper.events(contentResolver, calendarDisplays, 0).also { events ->
//                            events.let { viewModel.submitEvents(it) }
//                        }
//                    }
                }
                CalendarLoader.RequestCode.InsertEvent -> {
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

    private fun bindNotificationListener() {
        bindService(Intent(this, NotificationListener::class.java), notificationListenerConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindNotificationListener() {
        unbindService(notificationListenerConnection)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeView() {
        viewBinding.frameLayoutLockOpen.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    OpenLock.x = event.x
                    OpenLock.y = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    viewBinding.frameLayoutRipple.showRipple()

                    val distance = sqrt((OpenLock.x - event.x).pow(2) + (OpenLock.y - event.y).pow(2))
                    var scale = abs(endRange - distance * 0.5F) / endRange

                    when {
                        scale >= 1F -> scale = 1F
                        scale < 0.75F -> scale = 0.75F
                    }

                    val alpha: Float = (scale - 0.75F) * 4

                    viewBinding.constraintLayout.alpha = alpha
                    viewBinding.constraintLayout.scaleX = scale
                    viewBinding.constraintLayout.scaleY = scale
                    viewBinding.imageViewLockOpen.alpha = alpha
                    viewBinding.imageViewLockOpen.scaleX = scale
                    viewBinding.imageViewLockOpen.scaleY = scale

                    outOfEndRange = distance * 1.25F > endRange * 0.75F
                }
                MotionEvent.ACTION_UP -> {
                    if (outOfEndRange)
                        finish()
                    else {
                        viewBinding.frameLayoutRipple.hideRipple()
                        viewBinding.constraintLayout.scale(1F, 300L)
                        viewBinding.imageViewLockOpen.scale(1F, 300L)
                    }
                }
            }

            true
        }

        viewBinding.imageViewCamera.setOnClickListener {
            Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                            //startActivity(intent) 무미건조한 카메라 기능

                            // 시스템 카메라.
                            // 검색.

                            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                    .format(Date()).toString() + ".jpg")
                            val uri = FileProvider.getUriForFile(this@MainActivity, "com.flow.android.kotlin.lockscreen.fileprovider", file)

                            ApplicationUtil.findCameraAppPackageName(this@MainActivity)?.let {
                                var intent: Intent? = null

                                try {
                                    intent = packageManager.getLaunchIntentForPackage(it)
                                } catch (ignored: Exception) {
                                    Timber.e(ignored)
                                    // 카메라 없으니 꺼지렴.
                                }

                                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                startActivity(intent)
                            } ?: run {
                                // 커스텀 카메라 실행. 권한 요청 여기서 필요. ㄴㄴ 그냥 꺼지라고 하셈.
                            }

//                            val mIntent = Intent()
//                            mIntent.setPackage("com.google.android.camera") // 이게 아닐가능성도 있네.
//                            val f = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()).toString() + ".jpg")
//                            mIntent.action = MediaStore.ACTION_IMAGE_CAPTURE
//                            val uri = FileProvider.getUriForFile(this@MainActivity,"com.flow.android.kotlin.lockscreen.fileprovider" , f)
//                            mIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                            startActivity(mIntent)
                        }
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {

                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest?,
                            token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }

        viewBinding.imageViewSettings.setOnClickListener {
            Intent(this, ConfigurationActivity::class.java).also {
                it.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)

                configurationActivityResultLauncher.launch(it)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewBinding.imageViewHighlight.setOnClickListener {
                torch.toggle()

                val color = when(torch.mode) {
                    Torch.Mode.On -> ContextCompat.getColor(this, R.color.yellow_A_200)
                    Torch.Mode.Off -> ContextCompat.getColor(this, R.color.black)
                    else -> throw IllegalArgumentException("Invalid mode")
                }

                if (it is ImageView)
                    it.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        } else
            viewBinding.imageViewHighlight.visibility = GONE

        val tabTexts = arrayOf(
                getString(R.string.main_activity_002),
                getString(R.string.main_activity_000),
                getString(R.string.main_activity_001),
                getString(R.string.main_activity_003)
        )

        viewBinding.viewPager2.adapter = FragmentStateAdapter(this)
        TabLayoutMediator(viewBinding.centerAlignedTabLayout, viewBinding.viewPager2) { tab, position ->
            tab.customView = layoutInflater.inflate(
                    R.layout.tab_custom_view,
                    viewBinding.root,
                    false
            )

            val textView = tab.customView?.findViewById<TextView>(R.id.text_view) ?: return@TabLayoutMediator

            textView.text = tabTexts[position]
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

        Handler(Looper.getMainLooper()).postDelayed({
            if (selectedTabIndex == 0) {
                viewBinding.centerAlignedTabLayout.getTabAt(selectedTabIndex)?.let {
                    animateSelectedTab(it)
                }
            } else
                viewBinding.centerAlignedTabLayout.getTabAt(selectedTabIndex)?.select()

            viewBinding.centerAlignedTabLayout.fadeIn(duration)
            viewBinding.viewPager2.fadeIn(duration)
        }, duration)
    }

    private fun animateSelectedTab(tab: TabLayout.Tab) {
        tab.view.scale(1.5F)
    }

    private fun animateUnselectedTab(tab: TabLayout.Tab) {
        tab.view.scale(1.0F)
    }

    private val configurationActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val configurationChange = result.data?.getParcelableExtra<ConfigurationChange>(
                    ConfigurationActivity.Name.ConfigurationChange
            ) ?: return@registerForActivityResult

            viewModel.refresh(configurationChange)
        }
    }

    private fun checkManageOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // todo show alert message for permission. rational
                val uri = Uri.fromParts("package", packageName, null)
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)

                startActivity(intent)
                handler.postDelayed(checkManageOverlayPermission, delayMillis)
            } else
                startService()
        } else
            startService()
    }

    private fun checkNotificationListenerEnabled() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)
    }

    private fun startService() {
        val intent = Intent(applicationContext, LockScreenService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent)
        else
            startService(intent)
    }

    private fun checkPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_CALENDAR
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        for (grantedPermissionResponse in report.grantedPermissionResponses) {
                            when (grantedPermissionResponse.permissionName) {
                                Manifest.permission.READ_CALENDAR -> viewModel.postCalendarDisplays()
                            }
                        }

                        for (deniedPermissionResponse in report.deniedPermissionResponses) {
                            when (deniedPermissionResponse.permissionName) {
                                Manifest.permission.READ_CALENDAR -> {/* todo 캘린더 프래그먼트에 권한 허용해야한다고 보이기. */
                                }
                            }
                        }

                        checkManageOverlayPermission()
                        checkNotificationListenerEnabled()
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
                            color = ContextCompat.getColor(this, R.color.unselected),
                            id = -20210513L,
                            modifiedTime = System.currentTimeMillis(),
                            priority = System.currentTimeMillis()
                    )
            )

            ConfigurationPreferences.putFirstRun(this, false)
        }
    }

    override fun onMemoDeleted(memo: Memo) {
        viewModel.deleteMemo(memo)
    }

    override fun onMemoInserted(memo: Memo) {
        viewModel.insertMemo(memo)
    }

    override fun onMemoUpdated(memo: Memo) {
        viewModel.updateMemo(memo.apply {
            priority = if (isDone)
                -System.currentTimeMillis()
            else
                modifiedTime
        })
    }

    override fun onPermissionAllowClick() {
        checkPermission()
        //checkManageOverlayPermission()
    }
}