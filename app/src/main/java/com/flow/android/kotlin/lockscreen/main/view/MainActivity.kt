package com.flow.android.kotlin.lockscreen.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.view.MotionEvent
import android.view.View.GONE
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.application.ApplicationUtil
import com.flow.android.kotlin.lockscreen.base.BaseActivity
import com.flow.android.kotlin.lockscreen.calendar.viewmodel.CalendarViewModel
import com.flow.android.kotlin.lockscreen.configuration.view.ConfigurationActivity
import com.flow.android.kotlin.lockscreen.configuration.viewmodel.ConfigurationChange
import com.flow.android.kotlin.lockscreen.databinding.ActivityMainBinding
import com.flow.android.kotlin.lockscreen.devicecredential.DeviceCredential
import com.flow.android.kotlin.lockscreen.devicecredential.RequireDeviceCredential
import com.flow.android.kotlin.lockscreen.lockscreen.service.LockScreenService
import com.flow.android.kotlin.lockscreen.main.adapter.FragmentStateAdapter
import com.flow.android.kotlin.lockscreen.main.torch.Torch
import com.flow.android.kotlin.lockscreen.main.view.MainActivity.Unlock.endRange
import com.flow.android.kotlin.lockscreen.main.view.MainActivity.Unlock.outOfEndRange
import com.flow.android.kotlin.lockscreen.main.viewmodel.MainViewModel
import com.flow.android.kotlin.lockscreen.main.viewmodel.Refresh
import com.flow.android.kotlin.lockscreen.memo.viewmodel.MemoViewModel
import com.flow.android.kotlin.lockscreen.permission._interface.OnPermissionAllowClickListener
import com.flow.android.kotlin.lockscreen.permission.view.PermissionRationaleDialogFragment
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences
import com.flow.android.kotlin.lockscreen.shortcut.viewmodel.ShortcutViewModel
import com.flow.android.kotlin.lockscreen.util.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import timber.log.Timber
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : BaseActivity(),
        OnPermissionAllowClickListener, RequireDeviceCredential<Unit> {
    private val duration = 300L
    private val torch: Torch by lazy {
        Torch(this)
    }

    private val viewBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()
    private val memoViewModel: MemoViewModel by viewModels()
    private val shortcutViewModel: ShortcutViewModel by viewModels()

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

    private object Unlock {
        var x = 0F
        var y = 0F
        const val endRange = 600F
        var outOfEndRange = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (ConfigurationPreferences.getShowOnLockScreen(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)

                val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager

                keyguardManager.requestDismissKeyguard(this, null)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
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
                calendarViewModel.postValue()
            }
        } else {
            startService()
            calendarViewModel.postValue()
        }

        initializeViews()
        registerLifecycleObservers()
        initializeActivityResultLaunchers()
        firstRun()
    }

    override fun onBackPressed() {
        if (onBackPressedDispatcher.hasEnabledCallbacks())
            onBackPressedDispatcher.onBackPressed()
        else
            viewBinding.frameLayoutRipple.forceRippleAnimation()
    }

    override fun onPause() {
        with(viewBinding.centerAlignedTabLayout.selectedTabPosition) {
            ConfigurationPreferences.putSelectedTabIndex(this@MainActivity, this)
        }

        super.onPause()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun restoreVisibility() {
        viewBinding.frameLayoutRipple.hideRipple()
        viewBinding.constraintLayout.scale(1F, duration)
        viewBinding.imageViewUnlock.scale(1F, duration)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeViews() {
        viewBinding.linearLayoutUnlock.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Unlock.x = event.x
                    Unlock.y = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    viewBinding.frameLayoutRipple.showRipple()

                    val distance = sqrt((Unlock.x - event.x).pow(2) + (Unlock.y - event.y).pow(2))
                    var scale = abs(endRange - distance * 0.45F) / endRange

                    when {
                        scale >= 1F -> scale = 1F
                        scale < 0.75F -> scale = 0.75F
                    }

                    val alpha: Float = (scale - 0.75F) * 4

                    viewBinding.constraintLayout.alpha = alpha
                    viewBinding.constraintLayout.scaleX = scale
                    viewBinding.constraintLayout.scaleY = scale
                    viewBinding.imageViewUnlock.alpha = alpha
                    viewBinding.imageViewUnlock.scaleX = scale
                    viewBinding.imageViewUnlock.scaleY = scale

                    outOfEndRange = distance * 1.25F > endRange * 0.75F
                }
                MotionEvent.ACTION_UP -> {
                    if (outOfEndRange) {
                        if (DeviceCredential.requireUnlock(this))
                            confirmDeviceCredential(Unit)
                        else
                            finish()
                    } else
                        restoreVisibility()
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

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {}

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

                getActivityResultLauncher(ConfigurationActivity.Name.ConfigurationChange)?.launch(it)
                overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewBinding.imageViewHighlight.setOnClickListener {
                torch.toggle()

                val color = when(torch.mode) {
                    Torch.Mode.On -> {
                        viewBinding.frameLayoutHighlight.showRipple()
                        ContextCompat.getColor(this, R.color.yellow_A_200)
                    }
                    Torch.Mode.Off -> {
                        viewBinding.frameLayoutHighlight.hideRipple()
                        ContextCompat.getColor(this, R.color.white)
                    }
                    else -> throw IllegalArgumentException("Invalid mode")
                }

                viewBinding.imageViewHighlight.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        } else
            viewBinding.imageViewHighlight.visibility = GONE

        TabLayoutInitializer.initialize(
                viewBinding.centerAlignedTabLayout,
                viewBinding.viewPager2,
                FragmentStateAdapter(this)
        )
    }

    private fun registerLifecycleObservers() {
        viewModel.refresh.observe(this, {
            it ?: return@observe

            when(it) {
                Refresh.Calendar -> calendarViewModel.callRefresh()
                Refresh.Memo -> memoViewModel.callRefresh()
                Refresh.Shortcut -> shortcutViewModel.callRefresh()
            }
        })
    }

    private fun initializeActivityResultLaunchers() {
        putActivityResultLauncher(
                DeviceCredential.Key.ConfirmDeviceCredential,
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                    finish()
                }
        )

        putActivityResultLauncher(
                ConfigurationActivity.Name.ConfigurationChange,
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val configurationChange = result.data?.getParcelableExtra<ConfigurationChange>(
                                ConfigurationActivity.Name.ConfigurationChange
                        ) ?: return@registerForActivityResult

                        viewModel.refresh(configurationChange)
                    }
                }
        )
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
                                Manifest.permission.READ_CALENDAR -> calendarViewModel.postValue()
                            }
                        }

                        for (deniedPermissionResponse in report.deniedPermissionResponses) {
                            when (deniedPermissionResponse.permissionName) {
                                Manifest.permission.READ_CALENDAR -> {/* todo 캘린더 프래그먼트에 권한 허용해야한다고 보이기. */
                                }
                            }
                        }

                        checkManageOverlayPermission()
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
            memoViewModel.insert(
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

    override fun onPermissionAllowClick() {
        checkPermission()
        //checkManageOverlayPermission()
    }

    override fun confirmDeviceCredential(value: Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DeviceCredential.confirmDeviceCredential(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissCancelled() {
                    super.onDismissCancelled()
                    restoreVisibility()
                }

                override fun onDismissError() {
                    super.onDismissError()
                    restoreVisibility()
                }

                override fun onDismissSucceeded() {
                    super.onDismissSucceeded()
                    finish()
                }
            })
        } else {
            DeviceCredential.confirmDeviceCredential(
                    this,
                    getActivityResultLauncher(DeviceCredential.Key.ConfirmDeviceCredential)
            )

            restoreVisibility()
        }
    }
}